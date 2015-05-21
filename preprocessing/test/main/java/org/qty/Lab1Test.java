package org.qty;

import static org.junit.Assert.*;

import org.junit.Test;

public class Lab1Test {

    @Test
    public void test() {
        String s = "203.145.207.188 - - [01/Feb/2015:00:00:00 +0800] \"GET /action?;act=view;uid=;pid=0005158462;cat=J,J_007,J_007_001,J_007_001_001;erUid=41ee27d6-5f83-b982-69f9-f378dc9fc11b; HTTP/1.1\" 302 160 \"-\" \"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0\"";
        assertEquals("41ee27d6-5f83-b982-69f9-f378dc9fc11b", Lab1.eruid(s));
    }

}
