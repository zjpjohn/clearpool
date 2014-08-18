package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.util.MemoryUtil;
import org.opensource.clearpool.util.ThreadProcessUtil;

import com.alibaba.druid.mock.MockConnection;
import com.alibaba.druid.mock.MockDriver;
import com.alibaba.druid.pool.DruidDataSource;

/**
 * Compare with other Database Pool.
 */
public class CompareWithWonderfulPoolCase extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 50;
	private int maxPoolSize = 50;
	private int threadCount = 100;
	private int loopCount = 5;
	private int LOOP_COUNT = 1000_000 / this.threadCount;

	private static AtomicLong physicalConnStat = new AtomicLong();

	public static class TestDriver extends MockDriver {
		public static TestDriver instance = new TestDriver();

		@Override
		public boolean acceptsURL(String url) throws SQLException {
			if (url.startsWith("jdbc:test:")) {
				return true;
			}
			return super.acceptsURL(url);
		}

		@Override
		public Connection connect(String url, Properties info)
				throws SQLException {
			physicalConnStat.incrementAndGet();
			// to support clearpool
			return new MockConnection(this, "jdbc:mock:case", info) {
				@Override
				public String getSchema() throws SQLException {
					return null;
				}
			};
		}
	}

	@Override
	public void setUp() throws Exception {
		MemoryUtil.printMemoryInfo();
		System.setProperty("org.clearpool.log.unable", "true");
		DriverManager.registerDriver(TestDriver.instance);
		this.driverClass = this.getClass().getName() + "$TestDriver";
		this.jdbcUrl = "jdbc:test:comparecase:";
		this.user = "1";
		this.password = "1";
		physicalConnStat.set(0);
	}

	public void test_clearpool() throws Exception {
		ClearPoolDataSource dataSource = new ClearPoolDataSource();
		dataSource.setCorePoolSize(this.minPoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setJdbcUser(this.user);
		dataSource.setJdbcPassword(this.password);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "clearpool", this.LOOP_COUNT,
					this.threadCount, physicalConnStat);
		}
		System.out.println();
	}

	public void test_druid() throws Exception {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setInitialSize(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setValidationQuery("select 1");
		dataSource.setTestOnBorrow(false);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "druid", this.LOOP_COUNT,
					this.threadCount, physicalConnStat);
		}
		System.out.println();
	}

	public void test_tomcat_jdbc() throws Exception {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		dataSource.setMaxIdle(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "tomcat-jdbc",
					this.LOOP_COUNT, this.threadCount, physicalConnStat);
		}
		System.out.println();
	}

}