package stubs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ClientExample extends Application{
	SocketChannel clientsocket;
	Label labTitle, labAge, labGenre, labRank;
	TextField txtAge, txtGenre, txtRank;
	Button btnSearch, btnConnect;
	TableView table;
	TableColumn titleCol, artistCol, countCol, lyricCol;
	
	void startClient(){
		Thread thread = new Thread() {
			@Override
			public void run() {
				try{
					clientsocket = SocketChannel.open();
					clientsocket.connect(new InetSocketAddress("192.168.102.13", 1104));
				}catch(Exception e) {
					if(clientsocket.isConnected()) {stopClient();}
					return;
				}
				receive();
			}
		};
		thread.start();
	}
	
	void stopClient() {
		try {
			Platform.runLater(()->{
				
			});
			if(clientsocket!=null && clientsocket.isConnected()) {
				clientsocket.close();
			}
		}catch(IOException e) {}
	}
	
	void receive() {
		while(true) {
			try {
				ArrayList<melonVO> list = new ArrayList<melonVO>();
				ByteBuffer byteBuffer = ByteBuffer.allocate(100*1024);
				clientsocket.read(byteBuffer);
				
				byteBuffer.flip();
				Charset charset = Charset.forName("UTF-8");
				String datas = charset.decode(byteBuffer).toString();
				System.out.println(datas);
				
				String[] line = datas.split("\n");
				
				for(int i=0; i<line.length; i++) {
					String[] rowdata = line[i].split(",");
					list.add(new melonVO(rowdata[0],rowdata[1],rowdata[2],rowdata[3]));
				}
				
				ObservableList<melonVO> melonList = FXCollections.observableArrayList(
					list
				);
				
				Platform.runLater(()->{
					table.setItems(melonList);
				});
				
			}catch(Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	void send(String data) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					Charset charset = Charset.forName("UTF-8");
					ByteBuffer byteBuffer = charset.encode(data);
					clientsocket.write(byteBuffer);
					System.out.println(data);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		BorderPane root = new BorderPane();
		root.setPrefSize(1600, 1000);
		
		HBox top = new HBox();
		top.setPrefSize(1600, 50);
		top.setPadding(new Insets(3,3,3,3));
		top.setSpacing(10);
		
		btnConnect = new Button("로그인");
		btnConnect.setPrefSize(200, 30);
		btnConnect.setStyle("-fx-font: 20 arial;");
		btnConnect.setOnAction(e->{
			if(btnConnect.getText().equals("연결")) {
				startClient();
				Platform.runLater(()->{
					btnConnect.setText("환영합니다");
					btnConnect.setDisable(true);
				});
			}
		});
		
		labTitle = new Label("Sunboard_Chart_Finder");
		labTitle.setPrefWidth(800);
		labTitle.setStyle("-fx-font: 45 arial;");
		labTitle.setAlignment(Pos.CENTER);
		
		top.getChildren().add(btnConnect);
		top.getChildren().add(labTitle);
		
		root.setTop(top);
		
		HBox center = new HBox();
		center.setPrefSize(700, 20);
		center.setPadding(new Insets(5,5,5,5));
		center.setSpacing(10);
		
		labAge = new Label("년대 :");
		labAge.setPrefSize(70, 30);
		labAge.setStyle("-fx-font: 24 arial;");
		labGenre = new Label("장르 :");
		labGenre.setPrefSize(70, 30);
		labGenre.setStyle("-fx-font: 24 arial;");
		labRank = new Label("순위 :");
		labRank.setPrefSize(70, 30);
		labRank.setStyle("-fx-font: 24 arial;");
		
		txtAge = new TextField();
		txtAge.setPrefSize(80, 30);
		txtAge.setStyle("-fx-font: 20 arial;");
		txtGenre = new TextField();
		txtGenre.setPrefSize(200, 30);
		txtGenre.setStyle("-fx-font: 20 arial;");
		txtRank = new TextField();
		txtRank.setPrefSize(70, 30);
		txtRank.setStyle("-fx-font: 20 arial;");
		
		btnSearch = new Button("검색");
		btnSearch.setPrefSize(120,30);
		btnSearch.setStyle("-fx-font: 20 arial;");
		btnSearch.setOnAction(e->send(txtAge.getText()+","+txtGenre.getText()+","+txtRank.getText()));
		
		center.getChildren().add(labAge);
		center.getChildren().add(txtAge);
		center.getChildren().add(labGenre);
		center.getChildren().add(txtGenre);
		center.getChildren().add(labRank);
		center.getChildren().add(txtRank);
		center.getChildren().add(btnSearch);
		
		root.setCenter(center);
		
		BorderPane bottom = new BorderPane();
		bottom.setPrefSize(1600, 900);
		
		table = new TableView();
		table.setEditable(false);
		countCol = new TableColumn("count");
		countCol.setPrefWidth(50);
		countCol.setCellValueFactory(new PropertyValueFactory("count"));
		countCol.setStyle("-fx-font: 13 arial;");
		titleCol = new TableColumn("title");
		titleCol.setPrefWidth(200);
		titleCol.setCellValueFactory(new PropertyValueFactory("title"));
		titleCol.setStyle("-fx-font: 13 arial;");
		artistCol = new TableColumn("artist");
		artistCol.setPrefWidth(200);
		artistCol.setCellValueFactory(new PropertyValueFactory("artist"));
		artistCol.setStyle("-fx-font: 13 arial;");
		lyricCol = new TableColumn("lyric");
		lyricCol.setPrefWidth(1150);
		lyricCol.setCellValueFactory(new PropertyValueFactory("lyric"));
		lyricCol.setStyle("-fx-font: 13 arial;");
		
		table.getColumns().addAll(countCol, titleCol, artistCol, lyricCol);
		
		bottom.setCenter(table);
		root.setBottom(bottom);
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Client");
		primaryStage.setOnCloseRequest(event->stopClient());
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}


