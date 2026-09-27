package com.hr.generator;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 生成器主界面控制器：连接数据库 → 勾选多张表 → 预览 → 手动点击生成。
 *
 * 批量选择：表列表每个单元格渲染为复选框，点击表名即勾选/取消勾选，
 * 无需按住 Ctrl，可同时勾选任意多张表一次性生成。
 *
 * 线程模型：所有数据库访问与文件写入都在同一个后台线程串行执行，
 * 避免 UI 阻塞，也避免多任务并发读写 selectedTables。
 */
public class IndexController {

  @FXML private TextField hostField;
  @FXML private TextField portField;
  @FXML private TextField databaseField;
  @FXML private TextField userField;
  @FXML private PasswordField passwordField;
  @FXML private TextField basePackageField;
  @FXML private TextField backendPathField;
  @FXML private TextField frontendPathField;
  @FXML private ListView<String> tableList;
  @FXML private TextArea columnArea;
  @FXML private TextArea logArea;
  @FXML private Button connectButton;
  @FXML private Button backendChooseButton;
  @FXML private Button frontendChooseButton;
  @FXML private Button previewButton;
  @FXML private Button generateButton;
  @FXML private Button selectAllButton;
  @FXML private Button clearAllButton;
  @FXML private CheckBox overwriteCheck;
  @FXML private RadioButton tsRadio;
  @FXML private RadioButton jsRadio;
  @FXML private Label previewLabel;

  private Stage stage;

  /** 每张表的勾选状态（表名 → 是否勾选）。 */
  private final Map<String, BooleanProperty> tableChecks = new LinkedHashMap<>();
  /** 当前勾选的表元数据（仅在 FX 线程更新，后台任务只读快照）。 */
  private final List<TableMeta> selectedTables = new ArrayList<>();

  /** 单线程后台执行器：连接/读元数据/渲染/生成全部串行，天然避免并发问题。 */
  private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
    Thread t = new Thread(r, "generator-worker");
    t.setDaemon(true);
    return t;
  });

  @FXML
  public void initialize() {
    // 后端/前端目录默认指向 generator 同级的 backend / frontend
    Path moduleRoot = GeneratorApplication.MODULE_DIR;
    backendPathField.setText(moduleRoot.resolve("../backend").normalize().toString());
    frontendPathField.setText(moduleRoot.resolve("../frontend").normalize().toString());

    // 表列表：每个单元格渲染为复选框，点击整行即勾选/取消，支持任意多张表
    tableList.setCellFactory(lv -> {
      CheckBox checkBox = new CheckBox();
      // 关键修复：CheckBox 默认开启助记符解析（mnemonicParsing=true），
      // 会把表名中的下划线当作 Alt 助记键标记吃掉：hr_announcement → hrannouncement。
      // 数据库表名必须原样展示，显式关闭。
      checkBox.setMnemonicParsing(false);
      // 复选框本身不响应鼠标，让点击穿透到单元格，由单元格统一切换勾选状态
      checkBox.setMouseTransparent(true);
      ListCell<String> cell = new ListCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if (empty) {
            setGraphic(null);
          } else {
            checkBox.setText(item);
            checkBox.selectedProperty().unbind();
            checkBox.selectedProperty().bind(checkFor(item));
            setGraphic(checkBox);
          }
        }
      };
      cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      cell.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
        String item = cell.getItem();
        if (item != null) {
          BooleanProperty p = checkFor(item);
          p.set(!p.get());
          e.consume();
        }
      });
      return cell;
    });

    generateButton.setDisable(true);
    previewButton.setDisable(true);
  }

  public void init(Stage stage) {
    this.stage = stage;
    // 前端语言切换时同步预览区标签
    if (jsRadio != null) {
      jsRadio.selectedProperty().addListener((obs, old, now) -> {
        previewLabel.setText(now
            ? "已勾选表的字段类型对照（数据库类型 → Java/JS 类型）:"
            : "已勾选表的字段类型对照（数据库类型 → Java/TS 类型）:");
      });
    }
  }

  /** 当前选择的前端语言："ts" 或 "js"。 */
  private String frontendLang() {
    return jsRadio != null && jsRadio.isSelected() ? "js" : "ts";
  }

  private String url() {
    return "jdbc:mysql://" + hostField.getText().trim() + ":" + portField.getText().trim()
        + "/" + databaseField.getText().trim()
        + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
  }

  /** 批量操作（全选/清空）置位期间不挂监听，结束后手动统一重载一次。 */
  private boolean quietMode = false;
  /** 记录已挂监听的表名，避免重复挂载。 */
  private final java.util.Set<String> listened = new java.util.HashSet<>();

  /** 获取（或创建）某张表的勾选状态；勾选变化时自动重新加载已勾选表的元数据。 */
  private BooleanProperty checkFor(String tableName) {
    BooleanProperty p = checkForQuiet(tableName);
    if (!quietMode && listened.add(tableName)) {
      p.addListener((obs, old, now) -> reloadCheckedTables());
    }
    return p;
  }

  /** 勾选状态变化后，串行重新读取所有已勾选表的元数据并刷新预览。 */
  private void reloadCheckedTables() {
    // 在 FX 线程采集输入与勾选快照，后台线程不再触碰 UI 控件
    String url = url();
    String user = userField.getText().trim();
    String password = passwordField.getText();
    List<String> snapshot = tableChecks.entrySet().stream()
        .filter(e -> e.getValue().get())
        .map(Map.Entry::getKey)
        .toList();
    worker.submit(() -> {
      try {
        List<TableMeta> metas = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String name : snapshot) {
          TableMeta meta = DatabaseMetadataReader.table(url, user, password, name);
          metas.add(meta);
          sb.append("== ").append(meta.tableName()).append(" → ").append(meta.className())
              .append("（").append(meta.comment()).append("）==\n");
          for (ColumnMeta c : meta.columns()) {
            sb.append("  ").append(c.fullType()).append("  ").append(c.name())
                .append(c.primaryKey() ? " [PK]" : "")
                .append("  →  Java ").append(c.javaType())
                .append("  // ").append(c.comment()).append("\n");
          }
          sb.append("\n");
        }
        Platform.runLater(() -> {
          selectedTables.clear();
          selectedTables.addAll(metas);
          columnArea.setText(sb.toString());
          previewButton.setDisable(metas.isEmpty());
          generateButton.setDisable(metas.isEmpty());
          log("已勾选 " + metas.size() + " 张表（可继续勾选，或直接预览/生成）。");
        });
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        Platform.runLater(() -> log("读取表结构出错: " + msg + "\n" + sw));
      }
    });
  }

  /** 一键勾选全部表（逐个切换 BooleanProperty，触发统一的元数据重载）。 */
  @FXML
  private void onSelectAll(ActionEvent event) {
    List<String> items = tableList.getItems();
    if (items == null || items.isEmpty()) {
      log("请先连接数据库。");
      return;
    }
    // quietMode 抑制监听挂载，批量置位后只统一重载一次
    quietMode = true;
    try {
      for (String name : items) {
        checkForQuiet(name).set(true);
      }
    } finally {
      quietMode = false;
    }
    reloadCheckedTables();
    log("已全选 " + items.size() + " 张表。");
  }

  /** 清空全部勾选。 */
  @FXML
  private void onClearAll(ActionEvent event) {
    List<String> items = tableList.getItems();
    if (items == null || items.isEmpty()) {
      return;
    }
    quietMode = true;
    try {
      for (String name : items) {
        checkForQuiet(name).set(false);
      }
    } finally {
      quietMode = false;
    }
    reloadCheckedTables();
    log("已清空勾选。");
  }

  /** 只创建/获取勾选状态，不挂重载监听（批量操作用，避免每张表都连一次数据库）。 */
  private BooleanProperty checkForQuiet(String tableName) {
    return tableChecks.computeIfAbsent(tableName, n -> new SimpleBooleanProperty(false));
  }

  @FXML
  private void onConnect(ActionEvent event) {
    String url = url();
    String user = userField.getText().trim();
    String password = passwordField.getText();
    runAsync(connectButton, "正在连接数据库...", () -> {
      List<String> tables = DatabaseMetadataReader.tables(url, user, password);
      Platform.runLater(() -> {
        tableList.setItems(FXCollections.observableArrayList(tables));
        tableChecks.clear();
        listened.clear();
        selectedTables.clear();
        columnArea.clear();
        previewButton.setDisable(true);
        generateButton.setDisable(true);
        log("连接成功，共 " + tables.size() + " 张表。点击表名勾选，可同时勾选多张表批量生成。");
      });
      return null;
    });
  }

  @FXML
  private void onChooseBackend(ActionEvent event) {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("选择 backend 目录");
    Path current = Paths.get(backendPathField.getText().trim());
    if (Files.isDirectory(current)) chooser.setInitialDirectory(current.toFile());
    var dir = chooser.showDialog(stage);
    if (dir != null) backendPathField.setText(dir.getAbsolutePath());
  }

  @FXML
  private void onChooseFrontend(ActionEvent event) {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("选择 frontend 目录");
    Path current = Paths.get(frontendPathField.getText().trim());
    if (Files.isDirectory(current)) chooser.setInitialDirectory(current.toFile());
    var dir = chooser.showDialog(stage);
    if (dir != null) frontendPathField.setText(dir.getAbsolutePath());
  }

  @FXML
  private void onPreview(ActionEvent event) {
    if (selectedTables.isEmpty()) {
      log("请先勾选至少一张表。");
      return;
    }
    // FX 线程采集输入 + 快照，后台线程只读快照
    List<TableMeta> tables = List.copyOf(selectedTables);
    String basePackage = basePackageField.getText().trim();
    String backendPath = backendPathField.getText().trim();
    String frontendPath = frontendPathField.getText().trim();
    boolean overwrite = overwriteCheck.isSelected();
    String lang = frontendLang();
    Path templateRoot = GeneratorApplication.MODULE_DIR.resolve("src/main/resources");
    runAsync(previewButton, "正在渲染模板（不写文件，前端语言 " + lang.toUpperCase() + "）...", () -> {
      StringBuilder sb = new StringBuilder();
      int total = 0;
      for (TableMeta table : tables) {
        try {
          // 字段类型对照清单：数据库类型 → Java 类型 / TS 类型
          sb.append("== ").append(table.tableName()).append("（").append(table.comment())
              .append("）字段类型对照 ==\n");
          for (ColumnMeta c : table.columns()) {
            sb.append("  ").append(c.name()).append("  ").append(c.fullType())
                .append("  →  Java ").append(c.javaType())
                .append(" / TS ").append(c.tsType()).append("\n");
          }
          List<TemplateEngine.GeneratedFile> files = TemplateEngine.render(
              table, basePackage + "." + table.moduleDir(),
              templateRoot, Paths.get(backendPath), Paths.get(frontendPath), basePackage, lang);
          sb.append("  → 共 ").append(files.size()).append(" 个文件：\n");
          for (TemplateEngine.GeneratedFile f : files) {
            boolean exists = Files.exists(f.target());
            sb.append("    [").append(f.engine()).append("] ").append(f.target())
                .append(exists ? "  (已存在，" + (overwrite ? "将覆盖" : "将跳过") + ")" : "").append("\n");
            total++;
          }
          sb.append("\n");
        } catch (Exception e) {
          sb.append("== ").append(table.tableName()).append(" 渲染失败 ==\n  ")
              .append(e.getMessage()).append("\n\n");
        }
      }
      final String text = sb.toString();
      final int n = total;
      Platform.runLater(() -> log("预览完成，共 " + n + " 个待生成文件（未写入）：\n" + text));
      return null;
    });
  }

  @FXML
  private void onGenerate(ActionEvent event) {
    if (selectedTables.isEmpty()) {
      log("请先勾选至少一张表。");
      return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "即将写入 " + selectedTables.size() + " 张表的生成文件。\n"
            + (overwriteCheck.isSelected() ? "覆盖模式：同名文件将被覆盖！" : "安全模式：已存在文件将跳过。"),
        ButtonType.OK, ButtonType.CANCEL);
    confirm.setHeaderText("确认生成");
    confirm.showAndWait().ifPresent(btn -> {
      if (btn == ButtonType.OK) {
        doGenerate();
      }
    });
  }

  private void doGenerate() {
    List<TableMeta> tables = List.copyOf(selectedTables);
    boolean overwrite = overwriteCheck.isSelected();
    String basePackage = basePackageField.getText().trim();
    String backendPath = backendPathField.getText().trim();
    String frontendPath = frontendPathField.getText().trim();
    String lang = frontendLang();
    Path templateRoot = GeneratorApplication.MODULE_DIR.resolve("src/main/resources");
    runAsync(generateButton, "正在生成文件（前端语言 " + lang.toUpperCase() + "）...", () -> {
      int written = 0, skipped = 0;
      StringBuilder sb = new StringBuilder();
      for (TableMeta table : tables) {
        try {
          List<TemplateEngine.GeneratedFile> files = TemplateEngine.render(
              table, basePackage + "." + table.moduleDir(),
              templateRoot,
              Paths.get(backendPath), Paths.get(frontendPath),
              basePackage, lang);
          for (TemplateEngine.GeneratedFile f : files) {
            if (TemplateEngine.write(f, overwrite)) {
              written++;
              sb.append("  写入: ").append(f.target()).append("\n");
            } else {
              skipped++;
              sb.append("  跳过(已存在): ").append(f.target()).append("\n");
            }
          }
        } catch (Exception e) {
          sb.append("  失败: ").append(table.tableName()).append(" - ").append(e.getMessage()).append("\n");
        }
      }
      final int w = written, s = skipped;
      final String text = sb.toString();
      Platform.runLater(() -> log("生成完成：写入 " + w + " 个，跳过 " + s + " 个。\n" + text
          + "\n请回复“已完成手动生成”以继续完善前后端。"));
      return null;
    });
  }

  private void runAsync(Button button, String status, AsyncJob job) {
    if (button != null) button.setDisable(true);
    log(status);
    worker.submit(() -> {
      try {
        return job.run();
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        Platform.runLater(() -> log("出错: " + msg + "\n" + sw));
        return null;
      } finally {
        if (button != null) Platform.runLater(() -> button.setDisable(false));
      }
    });
  }

  interface AsyncJob {
    Void run() throws Exception;
  }

  private void log(String message) {
    logArea.appendText(message + "\n");
  }
}
