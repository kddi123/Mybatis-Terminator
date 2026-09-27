package com.hr.generator.plugin.ui;

import com.hr.generator.ColumnMeta;
import com.hr.generator.DatabaseMetadataReader;
import com.hr.generator.TableMeta;
import com.hr.generator.TemplateEngine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器主面板（Swing，运行在 IDEA 工具窗口里）。
 * 数据库访问与模板渲染都放到后台线程，UI 更新回到 EDT。
 */
public class GeneratorPanel extends JPanel {

    private final Project project;

    private final JBTextField hostField = new JBTextField("localhost");
    private final JBTextField portField = new JBTextField("3306");
    private final JBTextField databaseField = new JBTextField("hr_management");
    private final JBTextField userField = new JBTextField("root");
    private final JPasswordField passwordField = new JPasswordField("root");
    private final JBTextField basePackageField = new JBTextField("com.hr");
    private final TextFieldWithBrowseButton backendPathField = new TextFieldWithBrowseButton();
    private final TextFieldWithBrowseButton frontendPathField = new TextFieldWithBrowseButton();
    private final JBCheckBox overwriteCheck = new JBCheckBox("覆盖已存在文件");
    private final JBRadioButton tsRadio = new JBRadioButton("TypeScript", true);
    private final JBRadioButton jsRadio = new JBRadioButton("JavaScript");

    private final DefaultListModel<String> tableModel = new DefaultListModel<>();
    private final JBList<String> tableList = new JBList<>(tableModel);
    private final JTextArea previewArea = new JTextArea();
    private final JTextArea logArea = new JTextArea();

    private final JButton connectButton = new JButton("测试连接并读取表");
    private final JButton selectAllButton = new JButton("全选");
    private final JButton clearAllButton = new JButton("清空选择");
    private final JButton previewButton = new JButton("预览（不写文件）");
    private final JButton generateButton = new JButton("开始生成");

    /** 最近一次勾选表对应的元数据，仅在 EDT 上读写。 */
    private final List<TableMeta> selectedTables = new ArrayList<>();

    public GeneratorPanel(Project project) {
        super(new BorderLayout(8, 8));
        this.project = project;
        String base = project.getBasePath() == null ? "" : project.getBasePath();
        backendPathField.setText(base.isEmpty() ? "" : Paths.get(base, "backend").toString());
        frontendPathField.setText(base.isEmpty() ? "" : Paths.get(base, "frontend").toString());
        backendPathField.addBrowseFolderListener(folderListener(project, "选择后端目录"));
        frontendPathField.addBrowseFolderListener(folderListener(project, "选择前端目录"));

        ButtonGroup langGroup = new ButtonGroup();
        langGroup.add(tsRadio);
        langGroup.add(jsRadio);
        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                reloadSelected();
            }
        });
        previewArea.setEditable(false);
        logArea.setEditable(false);
        previewButton.setEnabled(false);
        generateButton.setEnabled(false);

        connectButton.addActionListener(e -> onConnect());
        selectAllButton.addActionListener(e -> selectAll());
        clearAllButton.addActionListener(e -> tableList.clearSelection());
        previewButton.addActionListener(e -> onPreview());
        generateButton.addActionListener(e -> onGenerate());

        add(buildForm(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 4, 2, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        addRow(form, c, 0, "主机", hostField, "端口", portField, "数据库", databaseField);
        addRow(form, c, 1, "用户", userField, "密码", passwordField, "", connectButton);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        form.add(new JBLabel("基础包名"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(basePackageField, c);
        c.gridx = 2;
        c.weightx = 0;
        form.add(new JBLabel("后端目录"), c);
        c.gridx = 3;
        c.gridwidth = 3;
        c.weightx = 1;
        form.add(backendPathField, c);

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        form.add(new JBLabel("前端目录"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        form.add(frontendPathField, c);
        c.gridwidth = 1;
        c.gridx = 4;
        c.weightx = 0;
        form.add(overwriteCheck, c);
        c.gridx = 5;
        JPanel lang = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        lang.add(tsRadio);
        lang.add(jsRadio);
        form.add(lang, c);
        return form;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row,
                        String l1, java.awt.Component f1, String l2, java.awt.Component f2,
                        String l3, java.awt.Component f3) {
        c.gridy = row;
        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0;
        form.add(new JBLabel(l1), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(f1, c);
        c.gridx = 2;
        c.weightx = 0;
        form.add(new JBLabel(l2), c);
        c.gridx = 3;
        c.weightx = 1;
        form.add(f2, c);
        c.gridx = 4;
        c.weightx = 0;
        form.add(new JBLabel(l3), c);
        c.gridx = 5;
        c.weightx = 1;
        form.add(f3, c);
    }

    private JSplitPane buildCenter() {
        JPanel left = new JPanel(new BorderLayout(4, 4));
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftButtons.add(selectAllButton);
        leftButtons.add(clearAllButton);
        left.add(new JBLabel("数据库表（Ctrl/Shift 多选）"), BorderLayout.NORTH);
        left.add(leftButtons, BorderLayout.SOUTH);
        left.add(new JBScrollPane(tableList), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(4, 4));
        JSplitPane texts = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JBScrollPane(previewArea), new JBScrollPane(logArea));
        texts.setResizeWeight(0.6);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(previewButton);
        actions.add(generateButton);
        right.add(actions, BorderLayout.NORTH);
        right.add(texts, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(240);
        return split;
    }

    private static TextBrowseFolderListener folderListener(Project project, String title) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(title);
        return new TextBrowseFolderListener(descriptor, project);
    }

    private String url() {
        return "jdbc:mysql://" + hostField.getText().trim() + ":" + portField.getText().trim()
                + "/" + databaseField.getText().trim()
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    }

    private String frontendLang() {
        return jsRadio.isSelected() ? "js" : "ts";
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void onConnect() {
        String url = url();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());
        connectButton.setEnabled(false);
        log("正在连接数据库...");
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return DatabaseMetadataReader.tables(url, user, password);
            }

            @Override
            protected void done() {
                connectButton.setEnabled(true);
                try {
                    List<String> tables = get();
                    tableModel.clear();
                    tables.forEach(tableModel::addElement);
                    selectedTables.clear();
                    previewArea.setText("");
                    previewButton.setEnabled(false);
                    generateButton.setEnabled(false);
                    log("连接成功，共 " + tables.size() + " 张表。");
                } catch (Exception e) {
                    log("连接失败: " + stack(e));
                }
            }
        }.execute();
    }

    private void selectAll() {
        if (!tableModel.isEmpty()) {
            tableList.setSelectionInterval(0, tableModel.size() - 1);
        }
    }

    private void reloadSelected() {
        List<String> names = tableList.getSelectedValuesList();
        String url = url();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword());
        new SwingWorker<List<TableMeta>, Void>() {
            @Override
            protected List<TableMeta> doInBackground() throws Exception {
                List<TableMeta> metas = new ArrayList<>();
                for (String name : names) {
                    metas.add(DatabaseMetadataReader.table(url, user, password, name));
                }
                return metas;
            }

            @Override
            protected void done() {
                try {
                    List<TableMeta> metas = get();
                    selectedTables.clear();
                    selectedTables.addAll(metas);
                    previewArea.setText(describe(metas));
                    boolean empty = metas.isEmpty();
                    previewButton.setEnabled(!empty);
                    generateButton.setEnabled(!empty);
                    log("已选择 " + metas.size() + " 张表。");
                } catch (Exception e) {
                    log("读取表结构出错: " + stack(e));
                }
            }
        }.execute();
    }

    private String describe(List<TableMeta> metas) {
        StringBuilder sb = new StringBuilder();
        for (TableMeta meta : metas) {
            sb.append("== ").append(meta.tableName()).append(" → ").append(meta.className())
                    .append("（").append(meta.comment()).append("）==\n");
            for (ColumnMeta col : meta.columns()) {
                sb.append("  ").append(col.fullType()).append("  ").append(col.name())
                        .append(col.primaryKey() ? " [PK]" : "")
                        .append("  →  Java ").append(col.javaType())
                        .append("  // ").append(col.comment()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void onPreview() {
        if (selectedTables.isEmpty()) {
            log("请先选择至少一张表。");
            return;
        }
        List<TableMeta> tables = List.copyOf(selectedTables);
        String basePackage = basePackageField.getText().trim();
        String backend = backendPathField.getText().trim();
        String frontend = frontendPathField.getText().trim();
        boolean overwrite = overwriteCheck.isSelected();
        String lang = frontendLang();
        previewButton.setEnabled(false);
        log("正在渲染模板（不写文件，前端语言 " + lang.toUpperCase() + "）...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return renderReport(tables, basePackage, backend, frontend, lang, overwrite, false);
            }

            @Override
            protected void done() {
                previewButton.setEnabled(true);
                try {
                    log(get());
                } catch (Exception e) {
                    log("预览失败: " + stack(e));
                }
            }
        }.execute();
    }

    private void onGenerate() {
        if (selectedTables.isEmpty()) {
            log("请先选择至少一张表。");
            return;
        }
        int answer = Messages.showYesNoDialog(project,
                "即将写入 " + selectedTables.size() + " 张表的生成文件。\n"
                        + (overwriteCheck.isSelected() ? "覆盖模式：同名文件将被覆盖！" : "安全模式：已存在文件将跳过。"),
                "确认生成", Messages.getQuestionIcon());
        if (answer != Messages.YES) {
            return;
        }
        List<TableMeta> tables = List.copyOf(selectedTables);
        String basePackage = basePackageField.getText().trim();
        String backend = backendPathField.getText().trim();
        String frontend = frontendPathField.getText().trim();
        boolean overwrite = overwriteCheck.isSelected();
        String lang = frontendLang();
        generateButton.setEnabled(false);
        log("正在生成文件（前端语言 " + lang.toUpperCase() + "）...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return renderReport(tables, basePackage, backend, frontend, lang, overwrite, true);
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);
                try {
                    String report = get();
                    log(report);
                    ApplicationManager.getApplication().invokeLater(() ->
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(false));
                } catch (Exception e) {
                    log("生成失败: " + stack(e));
                }
            }
        }.execute();
    }

    /** 渲染全部模板；write 为 true 时落盘，否则只返回预览报告。 */
    private String renderReport(List<TableMeta> tables, String basePackage, String backend, String frontend,
                                String lang, boolean overwrite, boolean write) throws Exception {
        Path templateRoot = TemplateResources.extract();
        Path backendRoot = Paths.get(backend);
        Path frontendRoot = Paths.get(frontend);
        StringBuilder sb = new StringBuilder();
        int written = 0;
        int skipped = 0;
        int planned = 0;
        for (TableMeta table : tables) {
            List<TemplateEngine.GeneratedFile> files = TemplateEngine.render(
                    table, basePackage + "." + table.moduleDir(),
                    templateRoot, backendRoot, frontendRoot, basePackage, lang);
            sb.append("== ").append(table.tableName()).append(" → ").append(files.size()).append(" 个文件 ==\n");
            for (TemplateEngine.GeneratedFile file : files) {
                if (!write) {
                    planned++;
                    boolean exists = Files.exists(file.target());
                    sb.append("  [").append(file.engine()).append("] ").append(file.target())
                            .append(exists ? "  (已存在，" + (overwrite ? "将覆盖" : "将跳过") + ")" : "")
                            .append("\n");
                } else if (TemplateEngine.write(file, overwrite)) {
                    written++;
                    sb.append("  写入: ").append(file.target()).append("\n");
                } else {
                    skipped++;
                    sb.append("  跳过(已存在): ").append(file.target()).append("\n");
                }
            }
        }
        if (write) {
            sb.insert(0, "生成完成：写入 " + written + " 个，跳过 " + skipped + " 个。\n");
        } else {
            sb.insert(0, "预览完成，共 " + planned + " 个待生成文件（未写入）：\n");
        }
        return sb.toString();
    }

    private static String stack(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
