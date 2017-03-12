package com.duowan.niejin.zookeeper.demo;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年2月26日
 *
 **/
public class CuratorTest extends TestCase {

	public CuratorTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(CuratorTest.class);
	}

	public CuratorFramework createClient(String namespace) {
		return CuratorFrameworkFactory.builder()
				.connectString("172.17.5.181:2181")
				.sessionTimeoutMs(30000)
				.connectionTimeoutMs(30000)
				.canBeReadOnly(false)
				.retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
				.namespace(namespace)
				.defaultData(null)
				.build();
	}

	public void testZkStart() {
		CuratorFramework client = this.createClient("curator2221");
		try{
			client.start();
			
			client.create().forPath("/demo11");
			
			LeaderLatch leadLatch = new LeaderLatch(client,  "/examples/leader","client1");
			leadLatch.start();
			System.out.println(leadLatch.hasLeadership());
			System.out.println(leadLatch.getId() + "-");
			//client.delete().forPath("demo");
			System.in.read();
			
			CloseableUtils.closeQuietly(leadLatch);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			CloseableUtils.closeQuietly(client);
		}
	}

	public void testWatch(){
		CuratorFramework client = this.createClient("curator");
		try{
			client.start();
			
			//client.delete().forPath("demo");
			System.out.println("create");
			client.create().withMode(CreateMode.EPHEMERAL).forPath("/test", "niejin".getBytes());
			System.out.println("watch");
			//client.getData().watched().inBackground().forPath("/test");
			client.getData().watched().forPath("/test");
			System.out.println("end");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			CloseableUtils.closeQuietly(client);
		}
	}
	public void testZkExample() {
		final String PATH = "/crud";
		CuratorFramework client = this.createClient(null);
		try {
			client.start();

			// create
			client.create().forPath(PATH, "ZK TEST EXAMPLE".getBytes());

			byte[] bs = client.getData().forPath(PATH);
			System.out.println("新建的节点，data为:" + new String(bs));

			client.setData().forPath(PATH, "I love football".getBytes());

			// 由于是在background模式下获取的data，此时的bs可能为null
			byte[] bs2 = client.getData().watched().inBackground().forPath(PATH);
			System.out.println("修改后的data为" + new String(bs2 != null ? bs2 : new byte[0]));

			client.delete().forPath(PATH);
			Stat stat = client.checkExists().forPath(PATH);

			// Stat就是对zonde所有属性的一个映射， stat=null表示节点不存在！
			System.out.println(stat);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableUtils.closeQuietly(client);
		}
	}

	// Curator 事务的支持
	public void testCuratorTransaction() {
		CuratorFramework client = this.createClient("CuratorTransaction");

		try {
			client.start();
			//开启事务
			CuratorTransaction transaction = client.inTransaction();
			
			Collection<CuratorTransactionResult> results = transaction.create().forPath("/demo","demo".getBytes())
						.and().setData().forPath("/demo1", "demo1".getBytes())
						.and().delete().forPath("/demo3")
						.and().commit();
			for(CuratorTransactionResult resut : results){
				System.out.println(resut.getForPath() + "-" +resut.getType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableUtils.closeQuietly(client);
		}

	}
	
	public void testCuratorListener(){
		CuratorFramework client = this.createClient("CuratorListener");
		try{
			
			client.start();
			
			client.create().creatingParentsIfNeeded().forPath("/demo/cnode","hello zk".getBytes());
			
			//在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
			ExecutorService pool = Executors.newFixedThreadPool(2);
			
			//监听数据节点的变化情况
			final NodeCache nodeCache = new NodeCache(client, "/demo/cnode", false);
			nodeCache.start(true);
			nodeCache.getListenable().addListener(new NodeCacheListener() {
				@Override
				public void nodeChanged() throws Exception {
					System.out.println("Node data is changed,new Data : " + new String(nodeCache.getCurrentData().getData()));
				}
			});
		
			//监听子节点的变化情况
			final PathChildrenCache  childrenCache = new PathChildrenCache(client, "/demo", true);
			childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
			childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
				
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch(event.getType()){
					case CHILD_ADDED :
					    System.out.println("CHILD_ADDED: " + event.getData().getPath());
						break;
					case CHILD_REMOVED:
					    System.out.println("CHILD_REMOVED: " + event.getData().getPath());
						break;
					case CHILD_UPDATED:
					    System.out.println("CHILD_UPDATED: " + event.getData().getPath());
						break;
					default:break;
					}
				}
			}, pool);
			
			client.setData().forPath("/demo/cnode", "zk Node data change".getBytes());
			
			Thread.sleep(10*1000);
			
			pool.shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			CloseableUtils.closeQuietly(client);
		}
	}
}
