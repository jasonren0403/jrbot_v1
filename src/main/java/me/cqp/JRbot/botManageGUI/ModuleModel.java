package me.cqp.JRbot.botManageGUI;

import me.cqp.JRbot.Utils.misc.webutils;

import javax.swing.table.DefaultTableModel;

public class ModuleModel extends DefaultTableModel {
    private ModuleModel(Object[][] data,Object[] column_names){
        super(data,column_names);
    }

    public ModuleModel() {
        //todo:replace this token to global run_token
        this(webutils.fetchTableData("bot_module",
                "COOLQ xxxx")
                ,new Object[]{"module_id", "是否为群应用", "是否为管理员专用", "开发者", "模块帮助",
                        "模块名称", "模块介绍", "其他配置项"});
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex != 0;
    }

    public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
        super.setValueAt(newValue,rowIndex,columnIndex);
        //todo: update database
    }
}
