package me.cqp.JRbot.botManageGUI;

import javax.swing.*;
import java.awt.*;

public class ConfigWindow extends JFrame {

    public ConfigWindow() {
        super("bot-个人设置");
        this.setSize(640, 480);
        this.setLayout(null);

        this.setResizable(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        configWindow();
        initComponent();
        this.setVisible(true);
    }

    private void configWindow(){
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int screenSizeHeight = (int) dimension.getHeight();
        int windowHeight = this.getHeight();
        int screenSizeWidth = (int) dimension.getWidth();
        int windowWidth = this.getWidth();
        this.setLocation(screenSizeWidth / 3 - windowWidth / 3,
                screenSizeHeight / 3 - windowHeight / 3);
    }

    private void initComponent(){
        JPanel panel = new JPanel(new BorderLayout());

        JTable table = new JTable(new ModuleModel());

        table.setRowHeight(30);
        table.setPreferredScrollableViewportSize(new Dimension(100,100));

        table.getTableHeader().setFont(new Font(null, Font.BOLD,12));  // 设置表头名称字体样式
        table.getTableHeader().setForeground(Color.BLACK);                // 设置表头名称字体颜色

        // 创建滚动面板，把 表格 放到 滚动面板 中（表头将自动添加到滚动面板顶部）
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new BorderLayout());

        JButton exit = new JButton("退出编辑");
        exit.addActionListener(event->System.exit(0));
        JButton refresh = new JButton("刷新");
        refresh.addActionListener(event->JOptionPane.showMessageDialog(null,"refresh button"));
//        refresh.setMargin(new Insets(20,20,20,20));
        buttonPanel.add(refresh,BorderLayout.WEST);
        buttonPanel.add(exit,BorderLayout.EAST);
        panel.add(table.getTableHeader(),BorderLayout.NORTH);
        panel.add(scrollPane);
        panel.add(table,BorderLayout.CENTER);
        panel.add(buttonPanel,BorderLayout.SOUTH);

        this.setContentPane(panel);
        this.pack();
    }
}
