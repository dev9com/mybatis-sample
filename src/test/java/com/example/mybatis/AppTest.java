package com.example.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest {

    @BeforeClass
    static public void testApp() {
        App.init();

        assertNotNull(App.factory);
    }

    SqlSession session = null;

    @Before
    public void setupSession() {
        session = App.factory.openSession();  // This obtains a database connection!
    }

    @After
    public void closeSession() {
        session.commit();  // This commits the data to the database. Required even if auto-commit=true
        session.close();   // This releases the connection
    }

    public TransactionToken tokenFactory(String tokenPrefix, String transactionPrefix)
    {
        TransactionToken t = new TransactionToken();
        t.setToken(tokenPrefix + System.currentTimeMillis());
        t.setTransaction(transactionPrefix + System.currentTimeMillis());
        return t;
    }

    @Test
    public void testInsert() {
        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        TransactionToken t = tokenFactory("alpha", "beta");
        mapper.insert(t);
        assertTrue(t.getId() > -1);

        long count = mapper.count();

        TransactionToken t2 = tokenFactory("cappa", "delta");
        mapper.insert(t2);
        assertTrue(t2.getId() > -1);

        assertEquals(count + 1, mapper.count());
    }

    @Test
    public void testUpdate() {
        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        TransactionToken t = tokenFactory("faraday", "gamma");
        mapper.insert(t);

        TransactionToken t2 = mapper.getById(t.getId());
        assertEquals(t.getToken(), t2.getToken());
        assertEquals(t.getTransaction(), t2.getTransaction());

        t2.setToken("bingo" + System.currentTimeMillis());
        t2.setTransaction("funky" + System.currentTimeMillis());
        mapper.update(t2);

        TransactionToken t3 = mapper.getById(t.getId());
        assertEquals(t2.getToken(), t3.getToken());
        assertEquals(t2.getTransaction(), t3.getTransaction());
    }

    @Test
    public void testDeleteById() {

        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        long count = mapper.count();

        TransactionToken t = tokenFactory("indigo", "jakarta");
        mapper.insert(t);
        assertEquals(count + 1, mapper.count());

        mapper.deleteById(t);
        assertEquals(count, mapper.count());


    }

    @Test
    public void testDeleteByTransaction() {
        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        long count = mapper.count();

        TransactionToken t2 = tokenFactory("kava", "lambda");
        mapper.insert(t2);
        assertEquals(count + 1, mapper.count());

        mapper.deleteByTransaction(t2);
        assertEquals(count, mapper.count());
    }

    @Test
    public void testFindByTransaction() {
        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        TransactionToken t = tokenFactory("manual", "nova");
        mapper.insert(t);
        assertTrue(t.getId() >= 0);

        TransactionToken t2 = mapper.selectByTransaction(t.getTransaction());
        assertEquals(t.getToken(), t2.getToken());
        assertEquals(t.getTransaction(), t2.getTransaction());
    }

    @Test
    public void testRollback() {
        TransactionTokenMapper mapper = session.getMapper(TransactionTokenMapper.class);

        long count = mapper.count();

        TransactionToken t = tokenFactory("omega", "passport");
        mapper.insert(t);
        assertEquals(count + 1, mapper.count());

        session.rollback();
        assertEquals(count, mapper.count());

        TransactionToken t3 = tokenFactory("quark", "star");
        mapper.insert(t3);
        assertEquals(count + 1, mapper.count());
    }
}
