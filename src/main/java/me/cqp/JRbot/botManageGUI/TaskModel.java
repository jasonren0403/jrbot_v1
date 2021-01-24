package me.cqp.JRbot.botManageGUI;

import me.cqp.JRbot.Utils.misc.webutils;

import javax.swing.table.DefaultTableModel;

public class TaskModel extends DefaultTableModel {
    private TaskModel(Object[][] data,Object[] column_names){
        super(data,column_names);
    }

    public TaskModel(){
        //todo:replace this token to global run_token
        this(webutils.fetchTableData("bot_task",
                "COOLQ xxxx"),
                new Object[]{"task_id", "任务名称", "任务描述", "是否为日常任务", "是否为一次性任务", "任务是否完成", "其他执行配置"});
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
