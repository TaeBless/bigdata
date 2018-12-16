package stubs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
	ExecutorService executorService;
	ServerSocketChannel serverSocketChannel;
	int port = 1104;
	List<Client> connectList = new Vector<Client>();

	public ChatServer() {
		executorService = Executors.newFixedThreadPool(10);

		try {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(true);
			InetSocketAddress inetSocketAddress = new InetSocketAddress("192.168.102.13", 1104);
			serverSocketChannel.bind(inetSocketAddress);
		} catch (Exception e) {
			if (serverSocketChannel.isOpen()) {
				stopServer();
			}
		}

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						SocketChannel socketChannel = serverSocketChannel.accept();
						Client client = new Client(socketChannel);
						connectList.add(client);
						/*
						 * if (connectList.size() > 1) { //client리스트 관리
						 * connectList.get(0).socketChannel.close();
						 * connectList.remove(0);
						 * System.out.println("기존의 클라이언트 연결 종료"); }
						 */
						System.out.println(client.socketChannel
								.getLocalAddress() + "가 연결되었습니다.");
						System.out.println("연결된 클라이언트 수 : "
								+ connectList.size());
					} catch (Exception e) {
						if (serverSocketChannel.isOpen()) {
							System.out.print("서버종료");
							stopServer();
						}
						break;
					}
				}
			}
		};
		System.out.println("waiting");
		executorService.submit(runnable);
	}

	void stopServer() {
		try {
			Iterator<Client> iterator = connectList.iterator();
			while (iterator.hasNext()) {
				Client client = iterator.next();
				client.socketChannel.close();
				iterator.remove();
			}
			if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
				serverSocketChannel.close();
			}
			if (executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
		} catch (Exception e1) {
			System.out.println("connected client doesn't exeist");
		}
	}

	public class Client {
		SocketChannel socketChannel;

		Client(SocketChannel socketChannel) throws IOException, InterruptedException {
			this.socketChannel = socketChannel;

			receive();
		}

		void receive() {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					while (true) {
						try {
							String datas = "";
							ByteBuffer byteBufferin = ByteBuffer.allocate(100);
							int readByteCount = socketChannel.read(byteBufferin);

							if (readByteCount == -1) {
								throw new IOException();
							}
							byteBufferin.flip();
							Charset charset = Charset.forName("UTF-8");
							String data = charset.decode(byteBufferin).toString();
							
							System.out.println(data);
							String[] parameters = data.split(",");
							String sql = "\"insert overwrite local directory 'melondata' select f.aa from (select concat(count(title),',',title,',',artist,',',lyric) as aa, count(title) as count from melon_chart " +
									"where substring(year,3,1)='" + parameters[0].substring(2,3) + "' and genre = '" + parameters[1] + "' and rank between 1 and " + parameters[2] + " " +
									"group by title, artist, lyric order by count desc limit 40) f\"";
							System.out.println(sql);

/*hive -e "insert overwrite local directory 'melondata' 
select concat(title,',',artist,',',count(title)) from melon_chart 
where substring(year,3,1)='9' and genre='Ballad' and 
rank between 1 and 3 group by title, artist limit 3"*/
							
							System.out.println("analysis Start...");
							
							Process pbanalysis = new ProcessBuilder().command("hive", "-e", sql, "&").start();
							String s = null;
							if (pbanalysis.waitFor() == 0) {
								Thread.sleep(1000);
								System.out.println("analysis finish");
								
								FileReader fr = new FileReader("/home/training/melondata/000000_0");
								BufferedReader br = new BufferedReader(fr);

								while((s=br.readLine())!=null){
									datas += s;
									datas += "\n";
								}
								System.out.println(datas);
								Charset charset1 = Charset.forName("UTF-8");
								ByteBuffer byteBufferout = ByteBuffer.allocate(100 * 1024);
								byteBufferout = charset1.encode(datas);
								for (Client client : connectList) {
									client.socketChannel.write(byteBufferout);
								}
								byteBufferout.flip();
							}
						}catch (Exception e) {
							try {
								connectList.remove(Client.this);
								socketChannel.close();
							} catch (IOException e2) {
								break;
							}
						}
					}

				}
			};
			executorService.submit(runnable);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("서버 시작");
		new ChatServer();
	}

}
