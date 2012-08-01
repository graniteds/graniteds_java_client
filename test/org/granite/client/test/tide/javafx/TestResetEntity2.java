package org.granite.client.test.tide.javafx;

import org.granite.client.test.tide.TestInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.javafx.JavaFXPlatform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestResetEntity2 {

    private ContextManager contextManager;
    private Context ctx;
    @SuppressWarnings("unused")
	private DataManager dataManager;
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXPlatform());
        contextManager.setInstanceStoreFactory(new TestInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
    }
    
    @Test
    public void testResetEntityEmbedded() {
        PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
        person.setAddress(new EmbeddedAddress("toto"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person);
        
        person.getAddress().setAddress("tutu");
        
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Person dirty", person.isDirty());
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Address reset", "toto", person.getAddress().getAddress());
        Assert.assertFalse("Context dirty", entityManager.isDirty());
        Assert.assertFalse("Person not dirty", person.isDirty());
    }
    
    @Test
    public void testResetEntityEmbeddedNested() {
        PersonEmbedNested person = new PersonEmbedNested(1L, 0L, "P1", null, "Toto");
        person.setAddress(new EmbeddedAddress2("toto"));
        person.getAddress().setLocation(new EmbeddedLocation("PARIS", "75020"));
        
        person = (PersonEmbedNested)entityManager.mergeExternalData(person);
        
        person.getAddress().getLocation().setZipcode("75019");
        
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Person dirty", person.isDirty());
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Location reset", "75020", person.getAddress().getLocation().getZipcode());
        Assert.assertFalse("Context dirty", entityManager.isDirty());
        Assert.assertFalse("Person not dirty", person.isDirty());
        
        person.setLastName("Truc");
        person.getAddress().setAddress("Bla");
        person.getAddress().getLocation().setCity("LONDON");
        
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Person dirty", person.isDirty());
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Location reset", "75020", person.getAddress().getLocation().getZipcode());
        Assert.assertEquals("Address reset", "toto", person.getAddress().getAddress());
        Assert.assertEquals("Person reset", "Toto", person.getLastName());
        Assert.assertFalse("Context dirty", entityManager.isDirty());
        Assert.assertFalse("Person not dirty", person.isDirty());
    }
    
}