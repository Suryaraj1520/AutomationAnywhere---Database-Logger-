package com.automationanywhere.botcommand.LoggerV2.commands;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.automationanywhere.commandsdk.annotations.*;
import com.automationanywhere.commandsdk.annotations.rules.NotEmpty;
import com.automationanywhere.commandsdk.model.AttributeType;
import com.automationanywhere.core.security.SecureString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.text.SimpleDateFormat;

//BotCommand makes a class eligible for being considered as an action.
@BotCommand
//CommandPks adds required information to be displayable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "Log", label = "[[Log.label]]",
        description = "[[log.description]]", icon = "pkg.svg",
        return_required = false)

//Identify the entry point for the action. Returns a Value<String> because the return type is String.
public class Log {
    private static Logger logger = LogManager.getLogger(Log.class);

    public static Connection conn = null;

    private static boolean closedStatus;

    public static ManageSession ManageSession;

    //Identify the entry point for the action. Returns a Value<String> because the return type is String.
    @Execute
    public void action(
            //Get Database server details
            @Idx(index = "1", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[Log.Sever.label]]", description = "[[log.Server.description]]")
            @NotEmpty SecureString Server,
            //Get Database Name
            @Idx(index = "2", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[Log.Database.label]]", description = "[[Log.Database.description]]")
            @NotEmpty SecureString Database,
            //Get database Username
            @Idx(index = "3", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[Log.User.label]]", description = "[[Log.User.description]]")
            @NotEmpty SecureString User,
            //Get Database Password
            @Idx(index = "4", type = AttributeType.CREDENTIAL)
            @Pkg(label = "[[Log.Password.label]]", description = "[[Log.Password.description]]")
            @NotEmpty SecureString Password,
            //Get Log Type
            @Idx(index = "5", type = AttributeType.RADIO, options = {
                    @Idx.Option(index = "5.1", pkg = @Pkg(label = "[[Log.region.5.1.label]]", value = "Trace")),
                    @Idx.Option(index = "5.2", pkg = @Pkg(label = "[[Log.region.5.2.label]]", value = "Info")),
                    @Idx.Option(index = "5.3", pkg = @Pkg(label = "[[Log.region.5.3.label]]", value = "Warning")),
                    @Idx.Option(index = "5.4", pkg = @Pkg(label = "[[Log.region.5.4.label]]", value = "Error")),
                    @Idx.Option(index = "5.5", pkg = @Pkg(label = "[[Log.region.5.5.label]]", value = "Fatal"))
            })
            @Pkg(label = "[[Log.region.label]]")
            @NotEmpty String LogType,
            //Get Log Message
            @Idx(index = "6", type = AttributeType.TEXT)
            @Pkg(label = "[[Log.Message.label]]")
            @NotEmpty String Message,
            //Get Execution ID
            @Idx(index = "7", type = AttributeType.TEXT)
            @Pkg(label = "[[Log.ExecutionID.label]]")
            @NotEmpty String ExecutionID,
            //Get Process ID
            @Idx(index = "8", type = AttributeType.TEXT)
            @Pkg(label = "[[Log.ProcessID.label]]")
            @NotEmpty String ProcessID,
            @Idx(index = "9", type = AttributeType.TEXT)
            //UI labels.
            @Pkg(label = "[[Log.ProcessName.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty String ProcessName,
            //Idx 4 would be displayed fourth, with a text box for entering the value.
            @Idx(index = "10", type = AttributeType.TEXT)
            //UI labels.
            @Pkg(label = "[[Log.TableName.label]]")
            //Ensure that a validation error is thrown when the value is null.
            @NotEmpty String TableName,
            //Get Task Name
            @Idx(index = "11", type = AttributeType.TEXT)
            @Pkg(label = "[[Log.Task.label]]")
            @NotEmpty String Task,
            //Get Text File Path
            @Idx(index = "12", type = AttributeType.FILE)
            @Pkg(label = "[[Log.LogTextFilepath.label]]")
            @NotEmpty String CSVLogFilepath
    ){

        String user = null;

        try {

            String UserName = User.getInsecureString();
            String Pass = Password.getInsecureString();
            String ServerName = Server.getInsecureString();
            String DatabaseName = Database.getInsecureString();

            Statement stmt = null;
            String sql;

            //loading jdbc driver
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            //connection string
            String url = "jdbc:sqlserver://" + ServerName + ";database=" + DatabaseName + ";encrypt=true;trustServerCertificate=true";

            try {
                try {
                    conn = DriverManager.getConnection(url, UserName, Pass);
                } catch (SQLException e) {
                    try {
                        if ((e.toString().contains("verify the connection properties"))) {
                            throw new RuntimeException(e);
                        }
                        conn.close();
                    } catch (SQLException ex) {
                        //
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Create the ProcessBuilder for the CMD command
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "whoami");

            // Start the process
            Process process = null;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                //
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String userName = null;
            try {
                userName = reader.readLine();
            } catch (IOException e) {
                //
            }
            int index = userName.lastIndexOf("\\");
            user = userName.substring(index + 1);

            //checking closed status
            try {
                closedStatus = conn.isClosed();
            } catch (SQLException e) {
                //
            }

            try {
                if (closedStatus == Boolean.TRUE) {
                    try {
                        conn = DriverManager.getConnection(url, UserName, Pass);

                        // Insert a record into the table
                        //  sql = "insert into [" +TableName+ "] values ('" + ExecutionID + "','" + ProcessID + "',GetDate(), (select @@SERVERNAME), (SELECT HOST_NAME()),'" + TableName + "','" + Task + "','" + LogType + "','" + Message + "',GetDate())";

                        sql = "insert into [" + TableName + "] ([ExecutionId],[ProcessId],[ProcessDate],[Machine],[User],[ProcessName],[TaskName],[LogType],[LogMessage],[CreatedDate]) values (" + ExecutionID + "," + ProcessID + ",GetDate(), (SELECT HOST_NAME()), '" + user + "' ,'" + ProcessName + "','" + Task + "','" + LogType + "','" + Message + "',GetDate())";

                        try {
                            stmt = conn.createStatement();
                            try {
                                stmt.executeUpdate(sql);
                            } catch (SQLException e) {
                                String QueryError = "insert into [" + TableName + "] ([ExecutionId],[ProcessId],[ProcessDate],[Machine],[User],[ProcessName],[TaskName],[LogType],[LogMessage],[CreatedDate]) values (" + ExecutionID + "," + ProcessID + ",GetDate(), (SELECT HOST_NAME()), '" + user + "' ,'" + ProcessName + "','" + Task + "','" + LogType + "','Syntax Error',GetDate())";
                                stmt.executeUpdate(QueryError);
                                throw e;
                            }
                            stmt.close();
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to log the details" + e);
                        }
                    } catch (SQLException e) {
                        throw new SQLException(e);
                    }
                } else {
                    // Insert a record into the table

                    sql = "insert into [" + TableName + "] ([ExecutionId],[ProcessId],[ProcessDate],[Machine],[User],[ProcessName],[TaskName],[LogType],[LogMessage],[CreatedDate]) values (" + ExecutionID + "," + ProcessID + ",GetDate(), (SELECT HOST_NAME()), '" + user + "' ,'" + ProcessName + "','" + Task + "','" + LogType + "','" + Message + "',GetDate())";

                    try {
                        stmt = conn.createStatement();
                        try {
                            stmt.executeUpdate(sql);
                        } catch (SQLException e) {
                            String QueryError = "insert into [" + TableName + "] ([ExecutionId],[ProcessId],[ProcessDate],[Machine],[User],[ProcessName],[TaskName],[LogType],[LogMessage],[CreatedDate]) values (" + ExecutionID + "," + ProcessID + ",GetDate(), (SELECT HOST_NAME()), '" + user + "' ,'" + ProcessName + "','" + Task + "','" + LogType + "','Syntax Error',GetDate())";
                            stmt.executeUpdate(QueryError);
                            throw e;
                        }
                        stmt.close();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to Log the details" + e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                conn.close();
            } catch (SQLException e) {
                //
            }
        } catch (RuntimeException e) {

            String Header = "ExecutionId, ProcessId, Processdate, Machine, User, ProcessName, TaskName, LogType, LogMessage, CreatedDate \n";

            Path path = Paths.get(CSVLogFilepath);

            // Check if the file exists
            if (!Files.exists(path)) {
                try {
                    BufferedWriter csvCreate = new BufferedWriter(new FileWriter(CSVLogFilepath));
                    csvCreate.write(Header);
                    csvCreate.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            java.util.Date currentDate = new Date();
            // Define a date format for formatting the date as a string
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            // Format the current date as a string
            String formattedDate = dateFormat.format(currentDate);

            // Create the ProcessBuilder for the CMD command
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "whoami");
            // Start the process
            Process process = null;
            try {
                process = processBuilder.start();
            } catch (IOException eX) {
                //
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String userName = null;
            try {
                userName = reader.readLine();
            } catch (IOException eU) {
                //
            }
            int index = userName.lastIndexOf("\\");
            user = userName.substring(index + 1);

            String HostName = null;
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                HostName = localHost.getHostName();
            } catch (Exception eH) {
                //
            }

            String Data = ExecutionID + ", " + ProcessID + ", " + formattedDate + ", " + HostName + ", " + user + ", " + ProcessName + ", " + Task + ", " + LogType + ", " + Message + ", " + formattedDate + "\n";

            String line = null;
            try {
                FileReader fileReader = new FileReader(CSVLogFilepath);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                line = bufferedReader.readLine();
                bufferedReader.close();
                fileReader.close();
            } catch (IOException ecsv) {
                ecsv.printStackTrace();
            }

            if (line.isEmpty()) {
                try {
                    FileWriter fileWriter = new FileWriter(CSVLogFilepath);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    // Write content to the file
                    bufferedWriter.write(Header);
                    bufferedWriter.newLine(); // Add a newline character
                    // Close the writer to save the file
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException ex) {
                    //
                }
            }

            try {
                FileWriter CSVfileWriter = new FileWriter(CSVLogFilepath,true);
                BufferedWriter CSVbufferedWriter = new BufferedWriter(CSVfileWriter);
                CSVbufferedWriter.write(Data);
                CSVbufferedWriter.close();
                CSVfileWriter.close();
            } catch (IOException eF) {
                //
            }
        }
    }
}
