package org.granite.tide.javafx.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.granite.persistence.javafx.PersistentSet;
import org.granite.tide.collections.ManagedPersistentCollection;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.impl.EntityManagerImpl;
import org.granite.tide.javafx.JavaFXDataManager;
import org.granite.tide.javafx.test.PersonEmbedColl.ContactList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("unchecked")
public class TestManagedEntity {
    
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        entityManager = new EntityManagerImpl("", new JavaFXDataManager(), null, null);
    }
    
    @Test
    public void testManagedEntity() {
        
        Person person = new Person(1L, 0L, "P1", null, null);
        person = (Person)entityManager.mergeExternalData(person);
        
        Person person2 = new Person(2L, 0L, "P2", null, null);
        person2 = (Person)entityManager.mergeExternalData(person2);
        
        Contact contact = new Contact(1L, 0L, "C1", null);
        entityManager.mergeExternalData(contact);
        
        final String[] changed = new String[1];
        
        person.firstNameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> property, String oldValue, String newValue) {
                changed[0] = "firstName";
            }            
        });
        person.lastNameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> property, String oldValue, String newValue) {
                changed[0] = "lastName";
            }            
        });
        contact.personProperty().addListener(new ChangeListener<Person>() {
            @Override
            public void changed(ObservableValue<? extends Person> property, Person oldValue, Person newValue) {
                changed[0] = "person";
            }            
        });
        
        person.setFirstName("Toto");
        Assert.assertEquals("String property change from null", "firstName", changed[0]);
        changed[0] = null;
        
        person.setFirstName(null);
        Assert.assertEquals("String property change to null", "firstName", changed[0]);
        changed[0] = null;
        
        person.setFirstName(null);
        Assert.assertNull("String property set without change", changed[0]);
        
        person.setFirstName("Toto");
        changed[0] = null;         
        person.setFirstName("Tutu");
        Assert.assertEquals("String property changed", "firstName", changed[0]);
        changed[0] = null;         
        
        contact.setPerson(person);
        Assert.assertEquals("Entity property changed", "person", changed[0]);
        changed[0] = null;
        
        contact.setPerson(person);
        Assert.assertNull("Entity property not changed", changed[0]);
        
        contact.setPerson(person2);
        Assert.assertEquals("Entity property changed", "person", changed[0]);
        changed[0] = null;
        
        contact.setPerson(null);
        Assert.assertEquals("Entity property changed to null", "person", changed[0]);
        changed[0] = null;
    }
    
    @Test
    public void testMergeEntity() throws Exception {
        
        Person person = new Person(1L, 0L, "P1", null, null);
        person = (Person)entityManager.mergeExternalData(person);
        
        Person person2 = new Person(1L, 0L, "P1", "Toto", "Toto");
        entityManager.mergeExternalData(person2);
        
        Assert.assertEquals("String property change from null", "Toto", person.getLastName());
    }
    
    @Test
    public void testMergeEntity2() throws Exception {
        
        Person person = new Person(1L, 0L, "P1", "Toto", "Toto");
        person.setContacts(new PersistentSet<Contact>(true));
        person = (Person)entityManager.mergeExternalData(person);
        
        Person person2 = new Person(1L, 0L, "P1", "Toto", "Toto");
        person2.setContacts(new PersistentSet<Contact>(true));
        Contact contact = new Contact(1L, 0L, "C1", "toto@toto.com");
        person2.getContacts().add(contact);
        entityManager.mergeExternalData(person2);
        
        Assert.assertEquals("List property merged", 1, person.getContacts().size());
        Assert.assertEquals("List property merged 2", "toto@toto.com", person.getContacts().get(0).getEmail());
    }
    
    @Test
    public void testMergeEntity3() throws Exception {
        
        Person person = new Person(1L, 0L, "P1", "Toto", "Toto");
        person.setContacts(new PersistentSet<Contact>(true));
        person = (Person)entityManager.mergeExternalData(person);
        
        Person person2 = new Person(1L, 0L, "P1", "Toto", "Toto");
        person2.setContacts(new PersistentSet<Contact>(true));
        Contact contact = new Contact(1L, 0L, "C1", "toto@toto.com");
        person2.getContacts().add(contact);
        entityManager.mergeExternalData(person2);
        
        Assert.assertEquals("List property merged", 1, person.getContacts().size());
        Assert.assertEquals("List property merged 2", "toto@toto.com", person.getContacts().get(0).getEmail());
    }
    
    @Test
    public void testMergeEntityCollection() {        
        
        Person p1 = new Person(1L, 0L, "P1", "A1", "B1");
        Person p2 = new Person(2L, 0L, "P2", "A2", "B2");
        ObservableList<Person> coll = FXCollections.observableArrayList(p1, p2, p2, p1);
        entityManager.mergeExternalData(coll);
        
        Person p1b = new Person(1L, 0L, "P1", "A1", "B1");
        Person p2b = new Person(2L, 0L, "P2", "A2", "B2");
        ObservableList<Person> coll2 = FXCollections.observableArrayList(p1b, p2b, p2b, p1b);
        entityManager.mergeExternalData(coll2, coll, null, null);
        
        Assert.assertEquals("Element 0", 1L, coll.get(0).getId().longValue());
        Assert.assertEquals("Element 1", 2L, coll.get(1).getId().longValue());
        Assert.assertEquals("Element 2", 2L, coll.get(2).getId().longValue());
        Assert.assertEquals("Element 3", 1L, coll.get(3).getId().longValue());
    }

    @Test
    public void testMergeCollection2() {
        
        ObservableList<Person> coll = FXCollections.observableArrayList(
                new Person(1L, 0L, "P1", "A1", "B1"),
                new Person(2L, 0L, "P2", "A2", "B2"),
                new Person(3L, 0L, "P3", "A3", "B3"),
                new Person(4L, 0L, "P4", "A4", "B4")
        );
        entityManager.mergeExternalData(coll);

        ObservableList<Person> coll2 = FXCollections.observableArrayList(
                new Person(5L, 0L, "P5", "A5", "B5"),
                new Person(4L, 0L, "P4", "A4", "B4"),
                new Person(3L, 0L, "P3", "A3", "B3"),
                new Person(2L, 0L, "P2", "A2", "B2")
        );
        entityManager.mergeExternalData(coll2, coll, null, null);
         
        Assert.assertEquals("Element 0", 5L, coll.get(0).getId().longValue());
        Assert.assertEquals("Element 2", 3L, coll.get(2).getId().longValue());
        Assert.assertEquals("Element 3", 2L, coll.get(3).getId().longValue());
    }
    
    @Test
    public void testMergeCollection3() {
        
        final List<String> changes = new ArrayList<String>();
        
        ObservableList<Person> coll = FXCollections.observableArrayList(
                new Person(1L, 0L, "P1", "A1", "B1"),
                new Person(2L, 0L, "P2", "A2", "B2"),
                new Person(3L, 0L, "P3", "A3", "B3"),
                new Person(4L, 0L, "P4", "A4", "B4")
        );
        coll.addListener(new ListChangeListener<Person>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Person> change) {
                while (change.next()) {
                    changes.add("c");
                }
            }            
        });
        entityManager.mergeExternalData(coll);
     
        ObservableList<Person> coll2 = FXCollections.observableArrayList(
                new Person(1L, 0L, "P1", "A1", "B1"),
                new Person(5L, 0L, "P5", "A5", "B5"),
                new Person(2L, 0L, "P2", "A2", "B2"),
                new Person(4L, 0L, "P4", "A4", "B4")
        );
        entityManager.mergeExternalData(coll2, coll, null, null);
     
        Assert.assertEquals("Element 1", 5L, coll.get(1).getId().longValue());
        Assert.assertEquals("Element 2", 2L, coll.get(2).getId().longValue());
        Assert.assertEquals("Events", 2, changes.size());
    }
    
    @Test
    public void testMergeCollection5() {
        
        final Map<String, Integer> changes = new HashMap<String, Integer>();
        changes.put("add", 0);
        changes.put("remove", 0);
        
        ObservableList<Person> coll = FXCollections.observableArrayList(
                new Person(1L, 0L, "P1", "A1", "B1"),
                new Person(2L, 0L, "P2", "A2", "B2"),
                new Person(3L, 0L, "P3", "A3", "B3")
        );
        coll.addListener(new ListChangeListener<Person>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Person> change) {
                while (change.next()) {
                    if (change.wasAdded())
                        changes.put("add", changes.get("add")+change.getAddedSize());
                    if (change.wasRemoved())
                        changes.put("remove", changes.get("remove")+change.getRemovedSize());
                }
            }            
        });
        
        ObservableList<Person> coll2 = FXCollections.observableArrayList(
                new Person(4L, 0L, "P4", "A4", "B4"),
                new Person(1L, 0L, "P1", "A1", "B1"),
                new Person(2L, 0L, "P2", "A2", "B2"),
                new Person(3L, 0L, "P3", "A3", "B3")
        );
        entityManager.mergeExternalData(coll2, coll, null, null);
        
        Assert.assertEquals("Element 1", 4L, coll.get(0).getId().longValue());
        Assert.assertEquals("Element 2", 1L, coll.get(1).getId().longValue());
        Assert.assertEquals("Element 3", 2L, coll.get(2).getId().longValue());
        Assert.assertEquals("Element 4", 3L, coll.get(3).getId().longValue());
        
        Assert.assertEquals("Event add count", 1, (int)changes.get("add")); 
        Assert.assertEquals("Event remove count", 0, (int)changes.get("remove")); 
    }
    
    @Test
    public void testMergeCollectionOfElements() {
        
        Person2 person = new Person2(1L, 0L, "P1", "Jacques", "Nicolas");
        person.setNames(FXCollections.observableArrayList("Jacques", "Nicolas"));
        entityManager.mergeExternalData(person);
         
        Person2 person2 = new Person2(1L, 1L, "P1", "Jacques", "Nicolas");
        person2.setNames(FXCollections.observableArrayList("Jacques", "Nicolas"));
        entityManager.mergeExternalData(person2);
         
        Assert.assertEquals("Collection merged", 2, person.getNames().size());
        
        Person2 person3 = new Person2(1L, 1L, "P1", "Jacques", "Nicolas");
        person3.setNames(FXCollections.observableArrayList("Jacques", "Nicolas", "Fran�ois"));
        entityManager.mergeExternalData(person3);
         
        Assert.assertEquals("Collection merged", 3, person.getNames().size());
    }
    
    @Test
    public void testMergeCollectionOfEntities() {
        
        Person person = new Person(1L, 0L, "P1", null, null);
        person.setContacts(new PersistentSet<Contact>());
        Contact c1 = new Contact(1L, 0L, "C1", "toto@toto.com");
        c1.setPerson(person);
        person.getContacts().add(c1);
        Contact c2 = new Contact(2L, 0L, "C2", "toto@toto.net");
        c2.setPerson(person);
        person.getContacts().add(c2);
        entityManager.mergeExternalData(person);
        
        Person person2 = new Person(1L, 0L, "P1", null, null);
        person2.setContacts(new PersistentSet<Contact>());
        Contact c21 = new Contact(1L, 0L, "C1", "toto@toto.com");
        c21.setPerson(person2);
        person2.getContacts().add(c21);
        Contact c22 = new Contact(2L, 0L, "C2", "toto@toto.net");
        c22.setPerson(person2);
        person2.getContacts().add(c22);
        Contact c23 = new Contact(3L, 0L, "C3", "toto@toto.org");
        c23.setPerson(person2);
        person2.getContacts().add(c23);
        entityManager.mergeExternalData(person2);
         
        Assert.assertEquals("Collection merged", 3, person.getContacts().size());
    }
    
    @Test
    public void testMergeMap() {
        ObservableMap<String, Person> map = FXCollections.observableHashMap();
        map.put("p1", new Person(1L, 0L, "P1", "A1", "B1"));
        map.put("p2", new Person(2L, 0L, "P2", "A2", "B2"));
        map.put("p3", new Person(3L, 0L, "P3", "A3", "B3"));
        entityManager.mergeExternalData(map);
         
        ObservableMap<String, Person> map2 = FXCollections.observableHashMap();
        map2.put("p1", new Person(1L, 0L, "P1", "A1", "B1"));
        map2.put("p3", new Person(3L, 0L, "P3", "A3", "B3"));
        map2.put("p4", new Person(4L, 0L, "P4", "A4", "B4"));
        map2.put("p5", new Person(5L, 0L, "P5", "A5", "B5"));
        entityManager.mergeExternalData(map2, map, null, null);
         
        Assert.assertEquals("Size", 4, map.size());
        Assert.assertEquals("Element 3", 3L, map.get("p3").getId().longValue());
        Assert.assertEquals("Element 4", 4L, map.get("p4").getId().longValue());
        Assert.assertEquals("Element 5", 5L, map.get("p5").getId().longValue());
    }
    
    @Test
    public void testMergeMap2() {
        ObservableMap<Salutation, Person> map = FXCollections.observableHashMap();
        map.put(Salutation.Dr, new Person(1L, 0L, "P1", "A1", "B1"));
        map.put(Salutation.Mr, new Person(2L, 0L, "P2", "A2", "B2"));
        map.put(Salutation.Ms, new Person(3L, 0L, "P3", "A3", "B3"));
        entityManager.mergeExternalData(map);
        
        ObservableMap<Salutation, Person> map2 = FXCollections.observableHashMap();
        map2.put(Salutation.Dr, new Person(1L, 0L, "P1", "A1", "B1"));
        map2.put(Salutation.Mr, new Person(3L, 0L, "P3", "A3", "B3"));
        map2.put(Salutation.Ms, new Person(4L, 0L, "P4", "A4", "B4"));
        entityManager.mergeExternalData(map2, map, null, null);
         
         Assert.assertEquals("Size", 3, map.size());
         Assert.assertEquals("Element Dr", 1L, map.get(Salutation.Dr).getId().longValue());
         Assert.assertEquals("Element Mr", 3L, map.get(Salutation.Mr).getId().longValue());
         Assert.assertEquals("Element Ms", 4L, map.get(Salutation.Ms).getId().longValue());
    }
    
    @Test
    public void testMergeMap3() {
        ObservableMap<String, Person> map = FXCollections.observableHashMap();
        map.put("p1", new Person(1L, 0L, "P1", "A1", "B1"));
        map.put("p2", new Person(2L, 0L, "P2", "A2", "B2"));
        map.put("p3", new Person(3L, 0L, "P3", "A3", "B3"));
        map.put("p4", new Person(4L, 0L, "P4", "A4", "B4"));
        entityManager.mergeExternalData(map);
        
        final Set<String> events = new HashSet<String>();
        
        map.addListener(new MapChangeListener<String, Person>() {
            @Override
            public void onChanged(MapChangeListener.Change<? extends String, ? extends Person> change) {
                if (change.wasAdded())
                    events.add("add:" + change.getKey());
                if (change.wasRemoved())
                    events.add("remove:" + change.getKey());
            }
        });
        
        ObservableMap<String, Person> map2 = FXCollections.observableHashMap();
        map2.put("p1", new Person(1L, 0L, "P1", "A1", "B1"));
        map2.put("p5", new Person(5L, 0L, "P5", "A5", "B5"));
        map2.put("p2", new Person(2L, 0L, "P2", "A2", "B2"));
        map2.put("p4", new Person(4L, 0L, "P4", "A4", "B4"));
        entityManager.mergeExternalData(map2, map, null, null);
        
        Assert.assertEquals("Element 5", 5L, map.get("p5").getId().longValue());
        Assert.assertEquals("Element 2", 2L, map.get("p2").getId().longValue());
        Assert.assertFalse("Element 3", map.containsKey("p3"));
        Assert.assertEquals("Events", 2, events.size());
    }
    
    @Test
    public void testMergeEntityEmbedded() {
        
        final Set<String> changes = new HashSet<String>();
    
        EmbeddedAddress a1 = new EmbeddedAddress("12 Main Street");
        PersonEmbed p1 = new PersonEmbed(1L, 0L, "P1", null, null);
        p1.setAddress(a1);
        p1.addressProperty().addListener(new ChangeListener<EmbeddedAddress>() {
            @Override
            public void changed(ObservableValue<? extends EmbeddedAddress> prop, EmbeddedAddress oldValue, EmbeddedAddress newValue) {
                changes.add("address");
            }             
        });
        a1.addressProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> prop, String oldValue, String newValue) {
                changes.add("address.address");
            }             
        });
        p1 = (PersonEmbed)entityManager.mergeExternalData(p1);
        
        EmbeddedAddress a2 = new EmbeddedAddress("14 Main Street");
        PersonEmbed p2 = new PersonEmbed(1L, 1L, "P1", null, null);
        p2.setAddress(a2);
        entityManager.mergeExternalData(p2);
        
        Assert.assertEquals("Embedded address merged", p1.getAddress(), a1);
        Assert.assertFalse("No event on Person", changes.contains("address"));
        Assert.assertEquals("Address updated", a1.getAddress(), a2.getAddress());
        Assert.assertTrue("Event on Address", changes.contains("address.address"));
    }

    @Test
    public void testMergeEmbeddedLazyCollection() {
        
        PersonEmbedColl p1 = new PersonEmbedColl(1L, 0L, "P1", null, null);
        p1.setContactList(new ContactList());
        p1.getContactList().setContacts(new PersistentSet<Contact>());
        Contact c1 = new Contact(1L, 0L, "C1", null);
        p1.getContactList().getContacts().add(c1);
         
        PersonEmbedColl p = (PersonEmbedColl)entityManager.mergeExternalData(p1);

        Assert.assertTrue("Contacts wrapped", p.getContactList().getContacts() instanceof ManagedPersistentCollection);
        Assert.assertEquals("Owner is person", p, ((ManagedPersistentCollection<Contact>)p.getContactList().getContacts()).getOwner());
        Assert.assertEquals("PropertyName", "contactList.contacts", ((ManagedPersistentCollection<Contact>)p.getContactList().getContacts()).getPropertyName());
    }
    
    @Test
    public void testMergeEntityLazy() {
        Person p1 = new Person(1L, 0L, "P1", "", "Test");
        entityManager.mergeExternalData(p1);
        
        Contact c1 = new Contact(1L, 0L, "C1", "test@test.com");
        Person p2 = new Person(1L, false);
        c1.setPerson(p2);
        
        ObservableList<Contact> coll = FXCollections.observableArrayList(c1);
        List<Contact> coll2 = (List<Contact>)entityManager.mergeExternalData(coll);
        Assert.assertEquals("Contact attached to person", p1, coll2.get(0).getPerson());
    }
    
    @Test
    // @Ignore("TODO")
    public void testMergeEntityMap() {
        PersonMap p = new PersonMap(1L, 0L, "P1", "Toto", "Toto");
        entityManager.mergeExternalData(p);
        
        p.setLastName("Test");
        
        PersonMap p2 = new PersonMap(1L, 1L, "P1", "Toto2", "Toto2");
        Map<String, EmbeddedAddress> testMap = new HashMap<String, EmbeddedAddress>();
        testMap.put("test", new EmbeddedAddress("test"));
        testMap.put("toto", new EmbeddedAddress("toto"));
        p2.setMapEmbed(FXCollections.observableMap(testMap));
        p = (PersonMap)entityManager.mergeExternalData(p2);
        
        Assert.assertNotNull("Map merged", p.getMapEmbed());
        Assert.assertEquals("Map size", 2, p.getMapEmbed().size());
        
        PersonMap p3 = new PersonMap(1L, 2L, "P1", "Toto3", null);
        testMap = new HashMap<String, EmbeddedAddress>();
        testMap.put("test", new EmbeddedAddress("test"));
        p3.setMapEmbed(FXCollections.observableMap(testMap));
        p = (PersonMap)entityManager.mergeExternalData(p3);
        
        Assert.assertNotNull("Map merged", p.getMapEmbed());
        Assert.assertEquals("Map size", 1, p.getMapEmbed().size());
    }
    
    @Test
    public void testMergeLazyEntity() {
        Person person = new Person(1L, false);
        Contact contact = new Contact(1L, 0L, "C1", null);
        contact.setPerson(person);
        
        entityManager.mergeExternalData(contact);
     
        Assert.assertFalse("Person not initialized", contact.getPerson().isInitialized());
        
        Person person2 = new Person(1L, 0L, "P1", "Jean", "Richard");
        Contact contact2 = new Contact(1L, 1L, "C1", null);
        contact2.setPerson(person2);
         
        entityManager.mergeExternalData(contact2);
        
        Assert.assertTrue("Person initialized", contact.getPerson().isInitialized());
        
        Person person3 = new Person(1L, false);
        Contact contact3 = new Contact(1L, 2L, "C1", null);
        contact3.setPerson(person3);
         
        entityManager.mergeExternalData(contact3);
         
        Assert.assertTrue("Person still initialized", contact.getPerson().isInitialized());
    }
    
    @Test
    public void testMergeLazyEntity2() {
        Contact contact = new Contact(1L, 0L, "C1", null);
        
        entityManager.mergeExternalData(contact);
         
        contact.setPerson(new Person());
        
        EntityManager tmp = entityManager.newTemporaryEntityManager();
        Contact c = (Contact)tmp.mergeFromEntityManager(entityManager, contact, null, true);
        
        Assert.assertTrue("Person initialized", c.getPerson().isInitialized());
    }
    
    @Test
    public void testMergeLazyEntity3() {
         User user = new User("toto", false);
         Group group = new Group("tutu");
         group.setUser(user);
         
         group = (Group)entityManager.mergeExternalData(group);
         
         Assert.assertFalse("User not initialized", group.getUser().isInitialized());
         
         Assert.assertNull("User not init 2", group.getUser().getName());
         
         User user2 = new User("toto");
         user2.setName("Jean Richard");
         Group group2 = new Group("tutu");
         group2.setUser(user2);
         
         entityManager.mergeExternalData(group2);
         
         Assert.assertTrue("User initialized", group.getUser().isInitialized());
    }
}
