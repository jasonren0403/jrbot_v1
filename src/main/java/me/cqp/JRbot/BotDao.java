package me.cqp.JRbot;

import me.cqp.JRbot.Utils.misc.dbutils;
import me.cqp.Jrbot;
import org.meowy.cqp.jcq.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class BotDao {
    public static boolean isWhiteListNeeded(long fromGroup){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try{
            conn = dbutils.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement("select need_whitelist from group_config where groupid = ?");
            pstmt.setLong(1,fromGroup);
            Jrbot.CQ.logDebug("DATABASE OPERATION", pstmt.toString());
            rs = pstmt.executeQuery();
            while(rs.next()){
                if(rs.getBoolean("need_whitelist")){
                    return rs.getBoolean("need_whitelist");
                }
            }
        }catch(SQLException sqlex){
            showSQLdebugMessages(sqlex,"GET whitelist status failed");
            return false;
        }finally{
            dbutils.CloseConnection(conn,pstmt,null,rs);
        }
        return false;
    }

    public static ArrayList<String> whitelist(long fromGroup){
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<String> str = new ArrayList<>();
        try {
            conn = dbutils.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement("select need_whitelist,whitelist from group_config where groupid=?");
            pstmt.setLong(1,fromGroup);
            rs = pstmt.executeQuery();
            while(rs.next()){
                if(rs.getBoolean("need_whitelist")){
                    String white = rs.getString("whitelist");
                    str = new ArrayList<>(Arrays.asList(white.split(",")));
                }
            }
        }catch(SQLException sqlex){
            showSQLdebugMessages(sqlex,"GET whitelist failed");
            return new ArrayList<>();
        }finally{
            dbutils.CloseConnection(conn,pstmt,null,rs);
        }
        return str;
    }

    public static void showSQLdebugMessages(SQLException sqlex, String message) {
        Jrbot.CQ.logWarning("Database::OperationError", message + StringUtils.lineSeparator + sqlex.getErrorCode() + ":" + sqlex.getSQLState() + StringUtils.lineSeparator + sqlex.toString());
    }

}
