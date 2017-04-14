package com.duowan.niejin.zookeeper.demo.discovery;
/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年3月15日
 *
**/
public class discorverMain {
	public static void main(String[] args) {/*

        CuratorFramework client = null;
        ServiceDiscover serviceDiscover = null;
        try{
            client = CuratorUtils.getCuratorClient();
            client.start();

            serviceDiscover = new ServiceDiscover(client, ServerApp.BASE_PATH);   //服务发现
            serviceDiscover.start();

            for(int i=0;i<10;i++){

                ServiceInstance<ServerPayload> instance = serviceDiscover.getServiceProvider(ServerApp.SERVICE_NAME);

                System.out.println("service:"+ServerApp.SERVICE_NAME+" instance id:"+instance.getId()+
                        ", name:"+instance.getName()+ ", address:"+instance.getAddress()+", port:"+instance.getPort());

                TimeUnit.SECONDS.sleep(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(serviceDiscover!=null){
                try {
                    serviceDiscover.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            client.close();
        }
    */}
}
