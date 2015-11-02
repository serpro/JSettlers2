/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * Copyright (C) 2003  Robert S. Thomas <thomas@infolab.northwestern.edu>
 * Portions of this file Copyright (C) 2010,2014 Jeremy D Monin <jeremy@nand.net>
 * Portions of this file Copyright (C) 2012 Paul Bilnoski <paul@bilnoski.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The maintainer of this program can be reached at jsettlers@nand.net
 **/
package soc.server;

import java.util.Vector;

import soc.disableDebug.D;
import soc.message.SOCServerPing;
import soc.server.genericServer.StringConnection;


/**
 * Pings the robots so that they know that they're connected
 * to the server
 *
 * @author Robert S Thomas
 */
public class SOCServerRobotPinger extends Thread
{
    private Vector<StringConnection> robotConnections;
    private int sleepTime = 150000;
    private SOCServerPing ping;
    private boolean alive;

    /**
     * Our server.
     * @since 1.1.11
     */
    @SuppressWarnings("unused")
    private final SOCServer srv;

    /**
     * Create a server robot pinger
     *
     * @param robots  the connections to robots; a Vector of {@link StringConnection}s
     */
    public SOCServerRobotPinger(SOCServer s, Vector<StringConnection> robots)
    {
        srv = s;
        robotConnections = robots;
        ping = new SOCServerPing(sleepTime);
        alive = true;
        setName ("robotPinger-srv");  // Thread name for debug
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void run()
    {
        while (alive)
        {
            if (!robotConnections.isEmpty())
            {
                for (StringConnection robotConnection : robotConnections)
                {
                    D.ebugPrintln("(*)(*)(*)(*) PINGING " + robotConnection.getData());
                    robotConnection.put(ping.toCmd());
                }
            }

            yield();

            try
            {
                sleep(sleepTime - 60000);
            }
            catch (InterruptedException exc) {}
        }

        //
        //  cleanup
        //
        robotConnections = null;
        ping = null;
    }

    /**
     * Cleanly exit the thread's run loop.
     */
    public void stopPinger()
    {
        alive = false;
    }

}  // public class SOCServerRobotPinger
