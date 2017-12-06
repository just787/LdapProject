import com.novell.ldap.*;
import com.novell.ldap.util.Base64;

import java.io.*;
import java.util.*;

/**
 * LDAP获取数据demo（各项配置在ldap.properties中）
 */
public class Main {
    // 将数据输出到文件的文件路径
    public static String outputFilePath = "./getLdapData.txt";
    public static String ldapHost = "192.168.1.123";
    public static String loginDN = "cn=Manager,dc=bsbpower,dc=com";
    public static String password = "secret";
    public static String searchBase = "dc=bsbpower,dc=com";
    public static String searchFilter = "objectClass=*";
    public static int version = LDAPConnection.LDAP_V3;
    public static String[] attrs = {};

    public static int ldapPort = LDAPConnection.DEFAULT_PORT;
    // 查询范围
    // SCOPE_BASE、SCOPE_ONE、SCOPE_SUB、SCOPE_SUBORDINATESUBTREE
    public static int searchScope = LDAPConnection.SCOPE_SUB;

    public static void main(String[] args) {
        System.out.println("**********************start");
        Map<String, String> configMap = getConfigMap();
        ldapHost = configMap.get("ldapHost");
        ldapPort = Integer.valueOf(configMap.get("ldapPort"));
        loginDN = configMap.get("loginDN");
        password = configMap.get("password");
        searchBase = configMap.get("searchBase");
        searchFilter = configMap.get("searchFilter");
        version = Integer.valueOf(configMap.get("version"));

        select();
        System.out.println("**********************end");
    }

    /**
     * 查询
     */
    public static void select() {
        LDAPConnection lc = new LDAPConnection();
        try {
            lc.connect(ldapHost, ldapPort);
            lc.bind(version, loginDN, password);//LDAPConnection.LDAP_V3  .getBytes("UTF8")

            LDAPSearchResults searchResults = lc.search(searchBase, searchScope, searchFilter, attrs, false);

            // 将数据输出到文件里
            File file = new File(outputFilePath);
            FileOutputStream fop = new FileOutputStream(file);
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }

                StringBuilder content = new StringBuilder();
                while (searchResults.hasMore()) {
                    LDAPEntry nextEntry = null;
                    nextEntry = searchResults.next();

                    //System.out.println("DN =: " + nextEntry.getDN());
                    //System.out.println("|---- Attributes list: ");

                    content.append("DN =: " + nextEntry.getDN() + "\r\n");
                    content.append("|---- Attributes list: " + "\r\n");

                    LDAPAttributeSet attributeSet = nextEntry.getAttributeSet();
                    Iterator<LDAPAttribute> allAttributes = attributeSet.iterator();
                    while (allAttributes.hasNext()) {
                        LDAPAttribute attribute = allAttributes.next();
                        String attributeName = attribute.getName();

                        Enumeration<String> allValues = attribute.getStringValues();
                        if (null == allValues) {
                            continue;
                        }
                        while (allValues.hasMoreElements()) {
                            String value = allValues.nextElement();
                            if (!Base64.isLDIFSafe(value)) {
                                // base64 encode and then print out
                                value = Base64.encode(value.getBytes());
                            }
                            //System.out.println("|---- ---- " + attributeName + " = " + value );
                            content.append("|---- ---- " + attributeName + " = " + value + "\r\n");
                        }
                    }
                }

                fop.write(content.toString().getBytes());
                fop.flush();
                fop.close();
                System.out.println("Done");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fop.close();
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        } finally {
            try {
                if (lc.isConnected()) {
                    lc.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取连接服务的配置值
     *
     * @return
     */
    public static Map<String, String> getConfigMap() {
        Map<String, String> map = new HashMap<String, String>();
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream("./ldap.properties"));
            pps.load(in);
            Enumeration en = pps.propertyNames(); //得到配置文件的名字

            while (en.hasMoreElements()) {
                String strKey = (String) en.nextElement();
                String strValue = pps.getProperty(strKey);
                map.put(strKey, strValue);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return map;
    }
}