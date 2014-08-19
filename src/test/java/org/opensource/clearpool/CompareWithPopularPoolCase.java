package org.opensource.clearpool;

import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.opensource.clearpool.core.ClearPoolDataSource;
import org.opensource.clearpool.log.PoolLogFactory;
import org.opensource.clearpool.util.MemoryUtil;
import org.opensource.clearpool.util.MockTestDriver;
import org.opensource.clearpool.util.ThreadProcessUtil;

import com.alibaba.druid.pool.DruidDataSource;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Compare with other Database Pool.
 */
public class CompareWithPopularPoolCase extends TestCase {
	private String jdbcUrl;
	private String user;
	private String password;
	private String driverClass;
	private int minPoolSize = 50;
	private int maxPoolSize = 50;
	private int threadCount = 100;
	private int loopCount = 5;
	private int LOOP_COUNT = 100_000 / this.threadCount;

	private static AtomicLong physicalCon = MockTestDriver.physicalCon;

	@Override
	public void setUp() throws Exception {
		MemoryUtil.printMemoryInfo();
		System.setProperty(PoolLogFactory.LOG_UNABLE, "true");
		DriverManager.registerDriver(new MockTestDriver());
		this.driverClass = MockTestDriver.CLASS;
		this.jdbcUrl = MockTestDriver.URL;
		this.user = "1";
		this.password = "1";
		physicalCon.set(0);
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
					this.threadCount, physicalCon);
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
					this.threadCount, physicalCon);
		}
		System.out.println();
	}

	public void test_dbcp() throws Exception {
		final BasicDataSource dataSource = new BasicDataSource();
		dataSource.setInitialSize(this.minPoolSize);
		dataSource.setMaxActive(this.maxPoolSize);
		dataSource.setMinIdle(this.minPoolSize);
		dataSource.setMaxIdle(this.maxPoolSize);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setDriverClassName(this.driverClass);
		dataSource.setUrl(this.jdbcUrl);
		dataSource.setPoolPreparedStatements(true);
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setTestOnBorrow(false);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "dbcp", this.LOOP_COUNT,
					this.threadCount, physicalCon);
		}
		System.out.println();
	}

	public void test_bonecp() throws Exception {
		BoneCPDataSource dataSource = new BoneCPDataSource();
		dataSource.setMinConnectionsPerPartition(this.minPoolSize);
		dataSource.setMaxConnectionsPerPartition(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setStatementsCacheSize(100);
		dataSource.setServiceOrder("LIFO");
		dataSource.setUsername(this.user);
		dataSource.setPassword(this.password);
		dataSource.setPartitionCount(1);
		dataSource.setAcquireIncrement(5);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "boneCP", this.LOOP_COUNT,
					this.threadCount, physicalCon);
		}
		System.out.println();
	}

	public void test_c3p0() throws Exception {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setMinPoolSize(this.minPoolSize);
		dataSource.setMaxPoolSize(this.maxPoolSize);
		dataSource.setDriverClass(this.driverClass);
		dataSource.setJdbcUrl(this.jdbcUrl);
		dataSource.setUser(this.user);
		dataSource.setPassword(this.password);
		for (int i = 0; i < this.loopCount; ++i) {
			ThreadProcessUtil.process(dataSource, "c3p0", this.LOOP_COUNT,
					this.threadCount, physicalCon);
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
					this.LOOP_COUNT, this.threadCount, physicalCon);
		}
		System.out.println();
	}

}
