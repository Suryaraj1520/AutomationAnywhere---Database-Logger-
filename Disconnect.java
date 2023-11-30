package com.automationanywhere.botcommand.LoggerV2.commands;

import com.automationanywhere.commandsdk.annotations.Execute;
import com.automationanywhere.commandsdk.annotations.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

//BotCommand makes a class eligible for being considered as an action.
@BotCommand
//CommandPks adds required information to be displayable on GUI.
@CommandPkg(
        //Unique name inside a package and label to display.
        name = "Disconnect", label = "[[Disconnect.label]]",
        description = "[[Disconnect.description]]", icon = "pkg.svg",
        //Return type information. return_type ensures only the right kind of variable is provided on the UI.
        return_required = false)

public class Disconnect {
    @Execute
    public void action()
//            @Idx(index = "1", type = AttributeType.SESSION)
//            @Pkg(label = "Logger Session", description = "Shared Global Session",
//                    default_value_type = DataType.SESSION)
//            //Using the sessionObject annotation here as it's a consumer class
//            @NotEmpty @SessionObject ManageSession manageSession) throws IOException {

    {
        try {
            Log ObjSession = new Log();
            ManageSession ManageSession = ObjSession.ManageSession;
            Connection conn = ObjSession.conn;

            try {
                ManageSession.close();
            } catch (IOException e) {
                //
            }

            try {
                if (conn.isValid(60)) {
                    conn.close();
                }
            } catch (SQLException ex) {
                //
            }
        } catch (Exception e) {
            //
        }
    }
    }
