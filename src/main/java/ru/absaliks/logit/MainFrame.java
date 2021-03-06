package ru.absaliks.logit;

import static java.util.Objects.nonNull;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.absaliks.logit.common.ResourceUtils;
import ru.absaliks.logit.service.ModbusService;
import ru.absaliks.logit.service.Service;
import ru.absaliks.logit.view.MainFrameController;

public class MainFrame extends Application {
  @Override
  public void start(Stage primaryStage) throws Exception {
    initStage(primaryStage);
    primaryStage.show();
  }
  
  private void initStage(Stage primaryStage) {
    // final MainFrameModel model = new MainFrameModel();
    final ModbusService modbusService = ModbusService.getInstance();
    final Service service = new Service(modbusService);

    FXMLLoader loader = new FXMLLoader();
    try {
      loader.setControllerFactory((c) -> new MainFrameController(service, modbusService, primaryStage));
      Parent root = loader.load(ResourceUtils.getInputStream("MainFrame.fxml"));
      primaryStage.setTitle("Logit");
      primaryStage.setScene(new Scene(root));
      setFrameIcon(primaryStage);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setFrameIcon(Stage primaryStage) {
    Image icon = ResourceUtils.getFrameIcon();
    if (nonNull(icon)) {
      primaryStage.getIcons().add(icon);
    }
  }
}
