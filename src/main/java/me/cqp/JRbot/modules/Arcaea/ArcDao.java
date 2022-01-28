package me.cqp.JRbot.modules.Arcaea;

import me.cqp.JRbot.Utils.webLogging;
import org.meowy.cqp.jcq.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;

import static me.cqp.JRbot.Utils.misc.dbutils.*;

public class ArcDao {
    public static ArcClientOffline getLoginCredential(int arcuid) {
        Connection conn;
        ArcClientOffline arcClient = null;
        ResultSet rs;
        PreparedStatement pstmt;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement("select * from jrbot.bot_arc_inner where arcuid=?");
            pstmt.setInt(1, arcuid);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int uid = rs.getInt("arcuid");
                String ucode = rs.getString("arcucode");
                String uname = rs.getString("arcuname");
                String authorization = rs.getString("authorization");
                String platform = rs.getString("platform");
                String deviceId = rs.getString("deviceId");
                boolean islocked = rs.getBoolean("islocked");
                boolean notuse = rs.getBoolean("doNotUseThisAccount");
                arcClient = new ArcClientOffline(uid, ucode, uname, authorization, deviceId, platform, islocked, notuse);
            }
        } catch (SQLException sqlex) {
            showSQLdebugMessages(sqlex, "error getting login credential!");
        } catch (ArcRuntimeException arc) {
            webLogging.addLog("error","ArcClientGet", arc.getMessage());
        }
        return arcClient;
    }

    public static ArrayList<ArcClientOffline> getUsers() {
        Connection conn = null;
        ArrayList<ArcClientOffline> list = new ArrayList<>();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from jrbot.bot_arc_inner");
            while (rs.next()) {
                int uid = rs.getInt("arcuid");
                String ucode = rs.getString("arcucode");
                String uname = rs.getString("arcuname");
                String authorization = rs.getString("authorization");
                String platform = rs.getString("platform");
                String deviceId = rs.getString("deviceId");
                boolean islocked = rs.getBoolean("islocked");
                boolean notuse = rs.getBoolean("doNotUseThisAccount");
                list.add(new ArcClientOffline(uid, ucode, uname, authorization, deviceId, platform, islocked, notuse));
            }
        } catch (SQLException sqlex) {
            showSQLdebugMessages(sqlex, "error getting login credential!");
        } finally {
            CloseConnection(conn, null, stmt, rs);
        }
        return list;
    }

    public static ArrayList<ArcClient> poolInit() {
        Connection conn = null;
        ArrayList<ArcClient> arcClients = new ArrayList<>();
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select arcuid,authorization,deviceId,platform from jrbot.bot_arc_inner where islocked = 0 and doNotUseThisAccount = 0");
            while (rs.next()) {
                int uid = rs.getInt("arcuid");
                String auth = rs.getString("authorization");
                String platform = rs.getString("platform");
                String deviceId = rs.getString("deviceId");
                arcClients.add(new ArcClient(platform, auth, deviceId, uid));
            }
        } catch (SQLException e) {
            showSQLdebugMessages(e, "error initing arcuserpool");
        } catch (ArcRuntimeException arc) {
            webLogging.addLog("error","ArcUserPoolInit", arc.getMessage());
        } finally {
            CloseConnection(conn, null, stmt, rs);
        }
        webLogging.addLog("debug","ArcDao", arcClients.toString());
        return arcClients;
    }

    public static boolean UpdateAccessToken(long arcuid, String accessToken) {
        if (accessToken.isEmpty()) return false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement("update bot_arc_inner set authorization = ? where arcuid = ?");
            pstmt.setString(1, accessToken);
            pstmt.setLong(2, arcuid);
            pstmt.executeUpdate();
            conn.commit();
            webLogging.addLog("info","ArcDao", String.format("Updated access token to %s",accessToken));
        } catch (SQLException sqlex) {
            showSQLdebugMessages(sqlex, "error updating access token!");
            return false;
        } finally {
            CloseConnection(conn, pstmt, null, null);
        }
        return true;
    }

    public static void showSQLdebugMessages(SQLException sqlex, String message) {
        String msg = message + StringUtils.lineSeparator +
                sqlex.getErrorCode() + ":" + sqlex.getSQLState() + StringUtils.lineSeparator + sqlex.toString();
        webLogging.addLog("error","ArcDao",msg);
    }
}
