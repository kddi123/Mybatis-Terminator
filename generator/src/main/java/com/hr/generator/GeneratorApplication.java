package com.hr.generator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Path;

/**
 * 生成器入口：加载 FXML/Index.fxml。
 * 运行方式：mvn javafx:run（工作目录 = generator/）
 */
public class GeneratorApplication extends Application {
  /** generator 模块根目录（自动识别，不依赖工作目录）。 */
  static final Path MODULE_DIR = ModulePaths.generatorModuleRoot();

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Index.fxml"));
    Parent root = loader.load();
    IndexController controller = loader.getController();
    controller.init(stage);
    stage.setTitle("HR 人事管理系统 - 代码生成器");
    stage.setScene(new Scene(root, 1100, 700));
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
