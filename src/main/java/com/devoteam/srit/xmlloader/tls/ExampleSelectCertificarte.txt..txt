    SSLServerSocketFactory              ssf;                                    // server socket factory
    SSLServerSocket                     skt;                                    // server socket

    // LOAD EXTERNAL KEY STORE
    KeyStore mstkst;
    try {
        String   kstfil=GlobalSettings.getString("javax.net.ssl.keyStore"        ,System.getProperty("javax.net.ssl.keyStore"        ,""));
        String   ksttyp=GlobalSettings.getString("javax.net.ssl.keyStoreType"    ,System.getProperty("javax.net.ssl.keyStoreType"    ,"jks"));
        char[]   kstpwd=GlobalSettings.getString("javax.net.ssl.keyStorePassword",System.getProperty("javax.net.ssl.keyStorePassword","")).toCharArray();

        mstkst=KeyStore.getInstance(ksttyp);
        mstkst.load(new FileInputStream(kstfil),kstpwd);
        }
    catch(java.security.GeneralSecurityException thr) {
        throw new IOException("Cannot load keystore ("+thr+")");
        }

    // CREATE EPHEMERAL KEYSTORE FOR THIS SOCKET USING DESIRED CERTIFICATE
    try {
        SSLContext        ctx=SSLContext.getInstance("TLS");
        KeyManagerFactory kmf=KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore          sktkst;
        char[]            blkpwd=new char[0];

        sktkst=KeyStore.getInstance("jks");
        sktkst.load(null,blkpwd);
        sktkst.setKeyEntry(svrctfals,mstkst.getKey(svrctfals,blkpwd),blkpwd,mstkst.getCertificateChain(svrctfals));
        kmf.init(sktkst,blkpwd);
        ctx.init(kmf.getKeyManagers(),null,null);
        ssf=ctx.getServerSocketFactory();
        }
    catch(java.security.GeneralSecurityException thr) {
        throw new IOException("Cannot create secure socket ("+thr+")");
        }

    // CREATE AND INITIALIZE SERVER SOCKET
    skt=(SSLServerSocket)ssf.createServerSocket(prt,bcklog,adr);
    ...
    return skt;
