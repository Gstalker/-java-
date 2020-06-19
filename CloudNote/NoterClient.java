import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class NoterClient extends Application{
  //���ڳ�ʼ���ȣ����
  public static final double ORIGINAL_HEIGHT = 600.0d;
  public static final double ORIGINAL_WIDTH = 580.0d;

  private String theEditingFileName;
  private long textLength = 0;
  private TextArea textArea;
  private TextField accountTF;
  private PasswordField passwordTF;
  private Button loginButton;
  private Button registerButton;

  private Stage mainWindow;//�����������

  private Stage getFileNameWindow;
  private String ipAddress = "localhost";

  //ȫ�ֱ�����:�ͷ���˵�����
  private Socket connector;
  private BufferedReader messageReader;
  private PrintWriter messageSender;
  public static void main(String[] args){
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception{
    //�ͻ��˵Ŀ�ʼ�ص�
    try{
      //�����������ͷ�����������
      this.connector = new Socket(ipAddress,19613);
      this.messageSender = new PrintWriter(
        this.connector.getOutputStream()
      );
      
      this.messageReader = new BufferedReader(
        new InputStreamReader(
          this.connector.getInputStream()
        )
      );
    }
    catch(Exception e){
      e.printStackTrace();
    }

    //�趨GUI
    this.mainWindow = primaryStage;
    this.setInitOptions(mainWindow);//�趨title��ͼ�꣬͸����
    this.setLoginWindowOptions(mainWindow);//�趨����������书��

    primaryStage.show();//չʾ��¼����

    primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {//�˳��¼�
      @Override
      public void handle(WindowEvent event){
        //�رճ����ʱ�򣬱��浱ǰ���ڱ༭���ļ��������жϺͷ�����������
        try{
          if(connector.isConnected()){
            if(theEditingFileName!=null)uploadFile();
            messageSender.println("exit");
            messageSender.flush();
            connector.close();
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
        finally{
          System.out.println("Noter exited");
          System.exit(0);
        }
      }
    });
  }

  private BufferedReader getBufferedReader(){
    return this.messageReader;
  }

  private PrintWriter getPrintWriter(){
    return this.messageSender;
  }

  private Stage getMainWindow(){
    return this.mainWindow;
  }

  private void setEditorWindowOptions(Stage primaryStage){
    //������setLoginWindowOptions
    //���ܣ��趨editor���ڵĲ���
    //������С�Ĵ�С
    primaryStage.setMinWidth(400.0d);
    primaryStage.setMinHeight(200.0d);
    //Editor���ڵĴ�С
    primaryStage.setWidth(ORIGINAL_HEIGHT);
    primaryStage.setHeight(ORIGINAL_WIDTH);
    primaryStage.setResizable(true);
    primaryStage.setScene(this.editorScene());
    this.recvLastData();
    this.setEditorMathod();
  }

  private void recvLastData(){
    //���� recvLastData()
    //���ܣ��ӿͻ��˽�����һ���û��༭��������
    try{
      PrintWriter pw = this.getPrintWriter();
      pw.println("recvLastData");
      pw.flush();
      BufferedReader br = this.getBufferedReader();
      this.theEditingFileName = br.readLine();
      String temp = br.readLine();
      String data = "";
      while(!temp.equals("0xbadbeef")){//�ļ�����
        data += temp+'\n';
        temp = br.readLine();
      }
      textLength = data.length();
      textArea.setText(data);
    }
    catch(Exception e){
      e.printStackTrace();
      alertWindow("fail", "Cannot read the recent file!");
    }
  }
  
  private void createNewFile(){
    System.out.println("createNewFile");
    try{
      uploadFile();
      System.out.println("file uploaded!");
      this.getFileNameWindow = new Stage();
      getFileNameWindow.setScene(setGetFileNameWindow());
      getFileNameWindow.setWidth(533.34);
      getFileNameWindow.setHeight(133.34);
      getFileNameWindow.setResizable(false);
      getFileNameWindow.initOwner(getMainWindow());
      getFileNameWindow.initModality(Modality.WINDOW_MODAL);
      getFileNameWindow.setTitle("Create New Note");
      getFileNameWindow.setOpacity(0.90d);
      getFileNameWindow.show();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private Scene setGetFileNameWindow(){
    BorderPane borderPane = new BorderPane();
    
    //���
    AnchorPane topFilling = new AnchorPane();
    Label tip = new Label("Please Input the name of the new file");
    AnchorPane.setTopAnchor(tip, 20.0d);
    AnchorPane.setLeftAnchor(tip, 50.0d);
    topFilling.setPrefHeight(30.0d);
    topFilling.getChildren().add(tip);
    borderPane.setTop(topFilling);
    
    //�ı���
    AnchorPane textAP = new AnchorPane();
    TextField getFileName = new TextField();
    AnchorPane.setLeftAnchor(getFileName,50.0);
    AnchorPane.setRightAnchor(getFileName,50.0);
    textAP.getChildren().add(getFileName);
    borderPane.setCenter(textAP);

    //��ť
    AnchorPane buttonAP = new AnchorPane();
    Button confirmButton = new Button("Confirm");
    AnchorPane.setLeftAnchor(confirmButton,402.44);
    AnchorPane.setRightAnchor(confirmButton,50.0);
    AnchorPane.setBottomAnchor(confirmButton, 10.0);
    buttonAP.getChildren().add(confirmButton);
    borderPane.setBottom(buttonAP);

    confirmButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        try{
          getFileName.setDisable(true);
          PrintWriter pw = getPrintWriter();
          BufferedReader br = getBufferedReader();
          pw.println("createNewNote");
          pw.flush();
          String newFileName = getFileName.getText();
          pw.println(newFileName);
          pw.flush();
          String result = br.readLine();
          if(result.equals("success")){
            theEditingFileName = newFileName+".txt";
            textArea.clear();
          }
          else if(result.equals("exist")){
            alertWindow("Fail!","The note have existed");
          }
          else if(result.equals("fail")){
            alertWindow("Fail!","Fail in Create New Note\nTry again!");
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
        finally{
          getFileNameWindow.close();
          getFileNameWindow = null;
        }
      }
    });

    return new Scene(borderPane);
  }

  private void uploadFile(){
    String file = this.textArea.getText();
    try{
      PrintWriter pw = getPrintWriter();
      pw.println("update");
      pw.flush();
      pw.println(theEditingFileName);
      pw.flush();
      pw.println(file);
      pw.flush();
      pw.println("0xbadbeef");
      pw.flush();
      BufferedReader br = getBufferedReader();
      System.out.println(br.readLine());
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  private void setEditorMathod(){
    //����setEditorMathod
    //���ܣ��趨�༭ģʽ�Ļ�������
    textArea.textProperty().addListener(new ChangeListener<String>() {
      //�趨textArea�е��ı�������
      //���û��޸��ı�����20���ַ���ʱ���ϴ��µ��ı���Ϣ��
      @Override
      public void changed(ObservableValue<? extends String> observable,String oldValue,String newValue){
        if(Math.abs(newValue.length() - textLength) > 20){
          uploadFile();
          textLength = newValue.length();
        }
      }
    });
  }

  private void setInitOptions(Stage primaryStage){
    //����setInitOptions
    //����:Ϊ�����趨��ʼ����
    primaryStage.setTitle("Cloud Note");
    primaryStage.getIcons().add(new Image("./sources/icon/white.jpg"));//����ͼ��
    primaryStage.setOpacity(0.95d);//����͸����
    //���ڳ���ʱ��λ�ã�����Ϊ���Ͻǵ�λ��
  }
  
  private void setLoginWindowOptions(Stage primaryStage){
    //������setLoginWindowOptions
    //���ܣ��趨��¼���ڵĲ���

    //�趨���ڳ���ʱ������Ļ���Ͻǵľ���
    primaryStage.setX(100);
    primaryStage.setY(100);

    //�趨���ڵĳ� �� ��
    primaryStage.setWidth(468.0d);
    primaryStage.setHeight(350.0d);

    //�趨���ھ�����������
    primaryStage.setScene(this.loginScene());

    //���ô����Ƿ���Ե�����С
    primaryStage.setResizable(false);

    //�趨��¼���ڵĵ�¼����
    this.setLoginMathod();
  }

  private void setLoginMathod(){
    loginButton.setOnAction(new EventHandler<ActionEvent>() {
      //�����������������������ʱ������������Щ����
      @Override
      public void handle(ActionEvent event){
        //��ȡ�˺�/�������е��ַ���
        String password = passwordTF.getText();
        String account = accountTF.getText();
        try{
          PrintWriter pw = getPrintWriter();
          //���߷��������ͻ���Ҫ��ʲô
          pw.println("login");
          pw.flush();

          //���û�������˻������뷢�͸�������
          pw.println(account);
          pw.flush();
          pw.println(password);
          pw.flush();

          //�ӷ�����Ǳ߽�����Ϣ����½�ɹ����
          BufferedReader br = getBufferedReader();
          String result = br.readLine();
          System.out.println("login "+result);
          if(result.equals("fail") || result.equals("")){//��½ʧ��ʱ�Ĵ���
            //��½ʧ�ܣ�������ʾ
            alertWindow("warning", "Login fail,Please try again");
          }
          else if(result.equals("success")){
            //��½�ɹ��Ժ󣬽���������
            setEditorWindowOptions(getMainWindow());
          }
        }
        catch(Exception e){
          e.printStackTrace();
          alertWindow("warning", "Login fail,Please try again");
        }
      }
    });
    registerButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        try{
          PrintWriter pw = getPrintWriter();
          BufferedReader br = getBufferedReader();
          pw.println("register");
          pw.flush();
          pw.println(accountTF.getText());
          pw.flush();
          pw.println(passwordTF.getText());
          pw.flush();
          String result = br.readLine();
          switch (result){
            case "exist":{
              alertWindow("Register fail", "This account has been existed!\nIf you forget the password,please connect Gstalker");
              break;
            }
            case "success":{
              infomationWindow("Congratulations", "Register success!\nPlease press the \"Login\" button");
              break;
            }
            case "fail":{
              alertWindow("Connection Error", "Cannot connect the server!");
              break;
            }
            default:{
              System.out.println("regisiterButton:UNDEFINED");
            }
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    });
  }

  private void alertWindow(String title,String message){
    //����һ�����洰��
    Alert alert = new Alert(AlertType.ERROR);//�趨����ͼ������
    alert.titleProperty().set(title);
    alert.headerTextProperty().set(message);
    alert.showAndWait();
  }
  private void infomationWindow(String title,String message){
    //����һ�����洰��
    Alert alert = new Alert(AlertType.INFORMATION);//�趨����ͼ������
    alert.titleProperty().set(title);
    alert.headerTextProperty().set(message);
    alert.showAndWait();
  }

  private Scene loginScene(){
    //������loginScene
    //���ܣ����ص�¼���ڵĳ���
    return new Scene(this.loginGUI());
  }

  private BorderPane loginGUI(){
    //���� loginGUI
    //���� ���ص�½���ڵ�BorderPane
    BorderPane BP = new BorderPane();
    
    //ʹ�ô��񲼾��������������˺ſ�
    //λ�ã��в�
    GridPane loginPane = new GridPane();
    //��ǩ��Account��Password
    Label accountLable = new Label("Account");
    Label passwordLabel = new Label("Password");
    //�˺�����������
    accountTF = new TextField();
    passwordTF = new PasswordField();
    
    //�򴰸񲼾���������
    loginPane.add(accountLable,0,0);
    loginPane.add(passwordLabel,0,1);
    loginPane.add(accountTF,1,0);
    loginPane.add(passwordTF,1,1);
    
    //���ö��뷽ʽ
    loginPane.setAlignment(Pos.CENTER);
    
    //���񲼾�ģʽ�У���������֮��ļ�϶����ֱ��ˮƽ��϶
    loginPane.setHgap(10.0d);
    loginPane.setVgap(10.0d);

    //��������񲼾ַ��ڴ��ڵ��м�λ��
    BP.setCenter(loginPane);

    //ʹ��ê�������������¼����
    //λ�ã��·�
    AnchorPane loginButtonAP = new AnchorPane();

    loginButton = new Button("Login");
    //���ø������AnchorPane�����о���ĳ����������ص�����
    AnchorPane.setLeftAnchor(loginButton,284.0d);
    AnchorPane.setRightAnchor(loginButton,100.0d);
    AnchorPane.setBottomAnchor(loginButton,30.0d);
    loginButtonAP.getChildren().add(loginButton);

    registerButton = new Button("Register");
    AnchorPane.setLeftAnchor(registerButton,100.0d);
    AnchorPane.setRightAnchor(registerButton,284.0d);
    AnchorPane.setBottomAnchor(registerButton,30.0d);
    loginButtonAP.getChildren().add(registerButton);
    BP.setBottom(loginButtonAP);


    //ʹ��ê����������Ϸ�
    AnchorPane topFiller = new AnchorPane();
    topFiller.setPrefHeight(150.0d);
    BP.setTop(topFiller);
    
    return BP;
  }

  private Scene editorScene(){
    //���� editorScene
    //���ܣ������ı��༭����Scene
    return new Scene(this.initGUI());
  }

  private AnchorPane initGUI(){
    //���� initGUI
    //���� �����ı��༭����AnchorPane
    AnchorPane noterPane = new AnchorPane();

    //�����Ϸ��˵���
    noterPane.getChildren().add(this.initMenu());
    //����ı��༭����
    noterPane.getChildren().add(this.initTextArea());
    return noterPane;
  }


  private MenuBar initMenu(){
    //������initMenu
    //���ܣ��������úõĲ˵�������
    MenuBar menuBar = new MenuBar();
    //�˵����ļ�����
    Menu fileOpt = new Menu("File");

    MenuItem openFile = new MenuItem("open");
    openFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        openFile();
      }
    });
    openFile.setAccelerator(KeyCombination.valueOf("CTRL+O"));

    MenuItem newFile = new MenuItem("new");
    newFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        createNewFile();
      }
    });
    newFile.setAccelerator(KeyCombination.valueOf("CTRL+N"));

    MenuItem saveFile = new MenuItem("save");
    saveFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        uploadFile();
      }
    });
    //���ÿ�ݼ�
    saveFile.setAccelerator(KeyCombination.valueOf("CTRL+S"));
    //�������ӵ�Menu��
    fileOpt.getItems().addAll(newFile,openFile,saveFile);

    //�˵����༭����
    Menu editOpt = new Menu("Edit");
    MenuItem copyEdit = new MenuItem("Copy");
    copyEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.copy();
      }
    });
    copyEdit.setAccelerator(KeyCombination.valueOf("CTRL+C"));

    MenuItem pasteEdit = new MenuItem("Paste");
    pasteEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.paste();
      }
    });
    pasteEdit.setAccelerator(KeyCombination.valueOf("CTRL+V"));

    MenuItem cutEdit = new MenuItem("cut");
    cutEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.cut();
      }
    });
    cutEdit.setAccelerator(KeyCombination.valueOf("CTRL+X"));

    MenuItem selectAllEdit = new MenuItem("Select ALL");
    selectAllEdit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event){
        textArea.selectAll();
      }
    });
    selectAllEdit.setAccelerator(KeyCombination.valueOf("CTRL+A"));
    editOpt.getItems().addAll(
      copyEdit,
      pasteEdit,
      cutEdit,
      selectAllEdit
    );

    //�˵����Ӿ���ʽ
    Menu viewOpt = new Menu("View");

    //�˵�������
    Menu helpOpt = new Menu("Help");
    MenuItem authorHelp = new MenuItem("Author's Blog");
    helpOpt.getItems().addAll(authorHelp);

    //�����߲���
    authorHelp.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent event) {
        HostServices host = getHostServices();
        host.showDocument("http://139.155.83.108/");
      }
    });

    //�˵���:������пؼ�
    menuBar.getMenus().addAll(fileOpt,editOpt,viewOpt,helpOpt);

    //���ò˵�����λ��
    AnchorPane.setLeftAnchor(menuBar, 0.0d);
    AnchorPane.setRightAnchor(menuBar, 0.0d);
    AnchorPane.setTopAnchor(menuBar, 0.0d);
    AnchorPane.setBottomAnchor(menuBar, 20.0d);
    return menuBar;
  }

  
  private TextArea initTextArea(){
    //������initTextArea
    //���ܣ������趨�õ�TextArea
    textArea = new TextArea();

    //������ʾ
    textArea.setPromptText("Write down your knowladge here ~");

    //�Ƿ�Ĭ�Ͼ۽��ڸò�����:��
    textArea.setFocusTraversable(false);

    //�ı��Զ�����
    textArea.setWrapText(true);

    //ȥ���߿�Ĭ����ʽ
    textArea.setStyle(
      "-fx-background-insets: 0;"+
      "-fx-focus-color: transparent;"+
      "-fx-padding: 0;"
    );
    
    //�����Զ�����
    AnchorPane.setLeftAnchor(textArea, 0.0d);
    AnchorPane.setRightAnchor(textArea,0.0d);

    //menuBar����Ĭ�ϸ߶�20.0���ص�
    AnchorPane.setTopAnchor(textArea, 20.0d);

    //�ײ���ʾ������20.0�����ص�
    AnchorPane.setBottomAnchor(textArea, 20.0d);
    return textArea;
  }

  private void openFile(){
    PrintWriter pw = getPrintWriter();
    BufferedReader br = getBufferedReader();
    uploadFile();
    try{
      pw.println("OpenFile");
      pw.flush();
      int totalFileCount = Integer.parseInt(br.readLine());
      List<String> files = new ArrayList<>();
      System.out.println("Available:");
      for(int i = 0; i < totalFileCount ; ++i){
        files.add(br.readLine());
        System.out.println("    "+files.get(i));
      }
      openFileWindow(files);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  private void openFileWindow(List<String> files){
    Stage openFileStage = new Stage();
    System.out.println("file uploaded!");
    openFileStage.setScene(setOpenFileWindow(openFileStage,files));
    openFileStage.setResizable(false);
    openFileStage.setX(100.0d);
    openFileStage.setY(100.0d);
    openFileStage.initOwner(getMainWindow());
    openFileStage.initModality(Modality.WINDOW_MODAL);
    openFileStage.setTitle("Open File");
    openFileStage.setOpacity(0.90d);
    openFileStage.show();
  }
  
  private Scene setOpenFileWindow(Stage stage,List<String> files){
    FlowPane fp = new FlowPane();
    fp.setPrefHeight(0.0d);
    fp.setOrientation(Orientation.VERTICAL);
    fp.setVgap(10.0d);
    fp.setAlignment(Pos.CENTER);
    for(int i = 0;i < files.size() ; ++i){
      Button file = new Button(files.get(i));
      fp.getChildren().add(file);
      file.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event){
          try{
            PrintWriter pw = getPrintWriter();
            BufferedReader br = getBufferedReader();
            pw.println(file.getText());
            pw.flush();
            theEditingFileName = file.getText();
            String file = readAllLine(br);
            System.out.println("Open file "+theEditingFileName);
            pw.println("success!");
            pw.flush();
            textArea.setText(file);
            stage.close();
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
      });
    }
    return new Scene(fp);
  }
  private String readAllLine(BufferedReader messageReader){
    //ȫ�Ķ�ȡһ���ļ�������String��ʽ����
    String result = "";
    try{
      String temp = messageReader.readLine();
      while(!temp.equals("0xbadbeef")){//�ļ�����
        result += temp+'\n';
        temp = messageReader.readLine();
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return result;
  }
}


