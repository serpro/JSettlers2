/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * Copyright (C) 2003  Robert S. Thomas <thomas@infolab.northwestern.edu>
 * Portions of this file Copyright (C) 2014 Jeremy D Monin <jeremy@nand.net>
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
package soc.message;

import java.util.StringTokenizer;


/**
 * This message is a request to create an account
 *<P>
 * The server will respond with a {@link SOCStatusMessage} indicating whether the account was created,
 * with status {@link SOCStatusMessage#SV_ACCT_CREATED_OK} or an error/rejection status and brief text.
 *<P>
 * In version 1.1.19 and higher, by default users must authenticate before creating user accounts.
 * (See {@link soc.util.SOCServerFeatures#FEAT_OPEN_REG}.)  If the user needs to log in but hasn't
 * before sending {@code SOCCreateAccount}, the server replies with {@link SOCStatusMessage#SV_PW_WRONG}.
 *
 * @author Robert S Thomas
 */
public class SOCCreateAccount extends SOCMessage
{
    private static final long serialVersionUID = 100L;  // last structural change v1.0.0 or earlier

    /**
     * symbol to represent a null email
     */
    private static String NULLEMAIL = "\t";

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Password
     */
    private String password;

    /**
     * Host name
     */
    private String host;

    /**
     * Email address
     */
    private String email;

    /**
     * Create a CreateAccount message.
     *
     * @param nn  nickname  Nickname (username) to give to requested account.
     *     The name must pass {@link SOCMessage#isSingleLineAndSafe(String)}
     *     in server v1.1.19 and higher.
     * @param pw  password; must not be null or ""
     * @param hn  host name; must not be null or ""
     * @param em  email
     * @throws IllegalArgumentException if {@code pw} or {@code hn} are null or empty ("")
     */
    public SOCCreateAccount(String nn, String pw, String hn, String em)
        throws IllegalArgumentException
    {
        if ((pw == null) || (pw.length() == 0))
            throw new IllegalArgumentException("pw");
        if ((hn == null) || (hn.length() == 0))
            throw new IllegalArgumentException("hn");

        messageType = CREATEACCOUNT;
        nickname = nn;
        password = pw;
        email = em;
        host = hn;
    }

    /**
     * Nickname (username) to give to requested account.
     * The name must pass {@link SOCMessage#isSingleLineAndSafe(String)}
     * in server v1.1.19 and higher.
     * @return the nickname
     */
    public String getNickname()
    {
        return nickname;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @return the host name
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the email address
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * CREATEACCOUNT sep nickname sep2 password sep2 host sep2 email
     *
     * @return the command String
     */
    public String toCmd()
    {
        return toCmd(nickname, password, host, email);
    }

    /**
     * CREATEACCOUNT sep nickname sep2 password sep2 host sep2 email
     *
     * @param nn  the nickname
     * @param pw  the password; must not be null or ""
     * @param hn  the host name; must not be null or ""
     * @param em  the email
     * @return    the command string
     * @throws IllegalArgumentException if {@code pw} or {@code hn} are null or empty ("")
     */
    public static String toCmd(String nn, String pw, String hn, String em)
        throws IllegalArgumentException
    {
        if ((pw == null) || (pw.length() == 0))
            throw new IllegalArgumentException("pw");
        if ((hn == null) || (hn.length() == 0))
            throw new IllegalArgumentException("hn");

        String tempem;

        if (em == null)
        {
            tempem = NULLEMAIL;
        }
        else
        {
            tempem = new String(em);

            if (tempem.equals(""))
            {
                tempem = NULLEMAIL;
            }
        }

        return CREATEACCOUNT + sep + nn + sep2 + pw + sep2 + hn + sep2 + tempem;
    }

    /**
     * Parse the command String into a CreateAccount message
     *
     * @param s   the String to parse
     * @return    a CreateAccount message, or null of the data is garbled
     */
    public static SOCCreateAccount parseDataStr(String s)
    {
        String nn;
        String pw;
        String hn;
        String em;

        StringTokenizer st = new StringTokenizer(s, sep2);

        try
        {
            nn = st.nextToken();
            pw = st.nextToken();
            hn = st.nextToken();
            em = st.nextToken();

            if (em.equals(NULLEMAIL))
            {
                em = "";
            }
        }
        catch (Exception e)
        {
            return null;
        }

        return new SOCCreateAccount(nn, pw, hn, em);
    }

    /**
     * @return a human readable form of the message
     */
    public String toString()
    {
        String s = "SOCCreateAccount:nickname=" + nickname + "|password=" + password + "|host=" + host + "|email=" + email;

        return s;
    }
}
