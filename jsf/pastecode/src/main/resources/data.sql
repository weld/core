insert into code (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (1, '2010-01-01 01:01:01', 'CSS', ' ', 'martin',
'div {
   min-height: 500px;
   height:auto !important;
   height: 500px;
}');
insert into code (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (2, '2009-01-02 01:01:01', 'CSS', ' ', 'peter',
'div {
   height: expression( this.scrollHeight < 501 ? "500px" : "auto" );
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (3, '2009-02-02 01:01:01', 'CSS', ' ', 'peter',
'a.GlobalOrangeButton span {
background: transparent url(http://media-sprout.com/tutorials/web/CSSSprit-SlideButton/images/button_left_orange.png) no-repeat 0 0;
display: block;
line-height: 22px;
padding: 7px 0 5px 18px;
color: #fff;
}
');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (4, '2009-02-02 01:01:01', 'JAVASCRIPT', ' ', 'john',
'var newPathname = "";
for ( i = 0; i pathArray.length; i++ ) {
  newPathname += "/";
  newPathname += pathArray[i];
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (5, '2009-02-03 01:01:01', 'JAVASCRIPT', ' ', 'graham',
'<script type="text/javascript">
<!--
    function toggle_visibility(id) {
       var e = document.getElementById(id);
');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (6, '2009-02-04 01:01:01', 'JAVASCRIPT', ' ', 'martin',
'var myArray = ["one", "two", "three"];

// console.log( myArray ) => ["one", "two", "three"]

myArray.length = 0;

// console.log( myArray ) => []');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (7, '2009-02-04 01:01:01', 'JAVASCRIPT', ' ', 'crazyman',
'function randRange(data) {
       var newTime = data[Math.floor(data.length * Math.random())];
       return newTime;
}

function toggleSomething() {
       var timeArray = new Array(200, 300, 150, 250, 2000, 3000, 1000, 1500);

       // do stuff, happens to use jQuery here (nothing else does)
       $("#box").toggleClass("visible");

       clearInterval(timer);
       timer = setInterval(toggleSomething, randRange(timeArray));
');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (8, '2009-02-05 01:01:01', 'JAVA', ' ', 'peter',
'public List<code> recentcodes()
    {
    	Query q = em.createQuery("SELECT c FROM code c WHERE hash=null ORDER BY datetime DESC ");
    	q.setMaxResults(MAX_codeS);
    	List<code> codes = q.getResultList();    	    	
    	return codes;
    }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (9, '2009-02-05 01:01:01', 'JAVA', ' ', 'graham',
' private void startOperation() throws HibernateException {
        session = HibernateFactory.openSession();
        tx = session.beginTransaction();
    }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (10, '2009-01-06 01:01:01', 'JAVA', ' ', 'martin',
'public List findAll() throws DataAccessLayerException{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from Event");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
        return events;
    }
');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (11, '2009-02-06 01:01:01', 'JAVA', ' ', 'crazyman',
'public Event find(Long id) throws DataAccessLayerException {
        Event event = null;
        try {
            startOperation();
            event = (Event) session.load(Event.class, id);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
        return event;
    }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (12, '2009-01-07 01:01:01', 'JAVA', ' ', 'graham',
' public void delete(Event event) throws DataAccessLayerException {
        try {
            startOperation();
            session.delete(event);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
    }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (13, '2009-01-08 01:01:01', 'JAVA', ' ', 'peter',
'public void create(Event event) throws DataAccessLayerException {
        try {
            startOperation();
            session.save(event);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateFactory.close(session);
        }
    }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (14, '2009-01-09 01:01:01', 'PHP', ' ', 'martin',
'function build_calendar($month,$year,$dateArray) {

     // Create array containing abbreviations of days of week.
     $daysOfWeek = array(S,M,T,W,T,F,S);

     // What is the first day of the month in question?
     $firstDayOfMonth = mktime(0,0,0,$month,1,$year);

     // How many days does this month contain?
     $numberDays = date(t,$firstDayOfMonth);
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (15, '2009-01-10 01:01:01', 'PHP', ' ', 'john',
'if ( !empty($_SERVER[HTTP_X_REQUESTED_WITH]) && strtolower($_SERVER[HTTP_X_REQUESTED_WITH]) == xmlhttprequest )
{
       # Ex. check the query and serve requested data
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (16, '2009-01-11 01:01:01', 'PHP', ' ', 'graham',
'<?php

function getTwitterStatus($userid){
$url = "http://twitter.com/statuses/user_timeline/$userid.xml?count=1";

$xml = simplexml_load_file($url) or die("could not connect");

       foreach($xml->status as $status){
       $text = $status->text;
       }
       echo $text;
 }

//my user id kenrick1991
getTwitterStatus("kenrick1991");

?>');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (17, '2009-01-11 01:01:01', 'PHP', ' ', 'peter',
'<?php
  header( Location: http://www.yoursite.com/new_page.html ) ;
?>');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (18, '2009-01-12 01:01:01', 'PHP', ' ', 'martin',
'function findexts ($filename) {

       $filename = strtolower($filename) ;

       $exts = split("[/\\.]", $filename) ;

       $n = count($exts)-1;

       $exts = $exts[$n];

       return $exts;

}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (19, '2009-01-12 01:01:01', 'PHP', ' ', 'graham',
'function fileRead($file){
   $lines = file($file);
   foreach ($lines as $line_num => $line) {
      echo  $line,  </br>;
   }
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (20, '2009-02-12 01:01:01', 'JAVA', ' ', 'peter',
'import java.util.*;

import org.hibernate.*;
import org.hibernate.criterion.*;

public class Main {
  
  
  public static void main(String[] args) {
    HibernateUtil.setup("create table EVENTS ( uid int, name VARCHAR, start_Date date, duration int);");
    
    // hibernate code start


        SimpleEventDao eventDao = new SimpleEventDao();
        Event event = new Event();
        event.setName("Create an Event");

        eventDao.create(event);
        
        Event foundEvent = eventDao.find(/*event.getId()*/1L);

        System.out.println(foundEvent.getName());
        

        HibernateUtil.checkData("select uid, name from events");        
    // hibernate code end
  }
  
}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (21, '2009-02-16 01:01:01', 'JAVA', ' ', 'crazyman',
'import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

public class DropModeON {
  public static void main(String[] args) {
    JPanel north = new JPanel();
    north.add(new JLabel("Drag from here:"));
    JTextField field = new JTextField(10);
    field.setDragEnabled(true); 
    north.add(field);

    final DefaultListModel listModel = new DefaultListModel();
    listModel.addElement("first");
    listModel.addElement("second");
    final JList list = new JList(listModel);
    list.setDragEnabled(true);

    list.setTransferHandler(new TransferHandler() {
      public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          return false;
        }
        JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
        if (dl.getIndex() == -1) {
          return false;
        } else {
          return true;
        }
      }');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (22, '2009-02-15 01:01:01', 'JAVA', ' ', 'martin',
'@Entity
public class Address {
    @Id
    private int id;
    private String street;
    private String city;
    private String state;
    private String zip;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String address) {
        this.street = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
    public String toString() {
        return "Address id: " + getId() + 
               ", street: " + getStreet() +
               ", city: " + getCity() +
               ", state: " + getState() +
               ", zip: " + getZip();
    }

}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (23, '2009-02-18 01:01:01', 'JAVA', ' ', 'graham',
'import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ProfessorService {
  protected EntityManager em;

  public ProfessorService(EntityManager em) {
    this.em = em;
  }

  public List findWithAlias() {
    Query query = em.createNativeQuery(
        "SELECT emp.emp_id AS emp_id, name, salary, manager_id, dept_id, address_id, "
            + "address.id, street, city, state, zip " + "FROM emp, address "
            + "WHERE address_id = id", "ProfessorWithAddressColumnAlias");
    return query.getResultList();
  }

}');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (24, '2009-02-19 01:01:01', 'JAVA', ' ', 'crazyman',
'@Entity
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String deptName) {
    this.name = deptName;
  }

  public String toString() {
    return "Department id: " + getId() + ", name: " + getName();
  }
}
');
insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (25, '2009-02-01 01:01:01', 'JAVA', ' ', 'graham',
'import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class JPAUtil {
  Statement st;
  
  public JPAUtil() throws Exception{
    Class.forName("org.hsqldb.jdbcDriver");
    System.out.println("Driver Loaded.");
    String url = "jdbc:hsqldb:data/tutorial";

    Connection conn = DriverManager.getConnection(url, "sa", "");
    System.out.println("Got Connection.");
    st = conn.createStatement();
  }
  public void executeSQLCommand(String sql) throws Exception {
    st.executeUpdate(sql);
  }
  public void checkData(String sql) throws Exception {
    ResultSet rs = st.executeQuery(sql);
    ResultSetMetaData metadata = rs.getMetaData();

    for (int i = 0; i < metadata.getColumnCount(); i++) {
      System.out.print("\t"+ metadata.getColumnLabel(i + 1)); 
    }
    System.out.println("\n----------------------------------");

    while (rs.next()) {
      for (int i = 0; i < metadata.getColumnCount(); i++) {
        Object value = rs.getObject(i + 1);
        if (value == null) {
          System.out.print("\t       ");
        } else {
          System.out.print("\t"+value.toString().trim());
        }
      }
      System.out.println("");
    }
  }
}');

insert into CodeFragment (ID, DATETIME, LANGUAGE, NOTE, USER, TEXT) values (26, '2009-02-01 01:01:01', 'JAVA', 'Some note', 'martin', 
'package org.jboss.weld.examples.pastie.session;

import java.util.List;

import javax.ejb.Stateful;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import org.jboss.weld.examples.pastie.model.code;
import javax.inject.*;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

/**
 * Session Bean implementation class HistoryBean
 */

@SessionScoped
@Named("history")
@Stateful
public class HistoryBean implements History, Serializable
{
    
	private static final long serialVersionUID = 20L;

	transient @Inject codeEAO eao;
	
	transient private @Inject PastieUtils utils;
	
	//transient private @Inject QueryInfo info;
	
	private QueryInfo info;
	
	

	private List<code> codes;
	
	private boolean firstAccess = true;
	
	private int TRIMMED_TEXT_LEN = 150; 
	
	private code searchItem;
	
	private int page = 0;
	


	public HistoryBean()	
    {
    }
	
	@PostConstruct
	public void initialize()
	{
		this.searchItem = new code();
		this.info = new QueryInfo();
	}
    
    public List<code> getcodes() 
    {
  	   return this.codes;  
    }
    
    public void loadcode()
    {
    	if (firstAccess)
    	{
	    	this.codes = eao.allcodes();
	        
	        for(int i=0; i!=this.codes.size();i++)
	        {
	     	   String s = this.codes.get(i).getText();
	     	   //this.codes.get(i).setText(s.substring(0, s.length() > TRIMMED_TEXT_LEN ? TRIMMED_TEXT_LEN : s.length()));
	        }   
	        firstAccess = false;
    	}
    }
    
    public void setcodes(List<code> codes) {
		this.codes = codes;
	}    
    
    @Produces @Named("searchItem")
	public code getSearchItem() {
		return searchItem;
	}

	public void setSearchItem(code searchItem) {
		this.searchItem = searchItem;
	}
	
	public String search()
	{
		this.codes = eao.searchcodes(this.searchItem, this.page, this.info);
		
		return "history";
	}
	

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	
	public QueryInfo getInfo() {
		return info;
	}

	public void setInfo(QueryInfo info) {
		this.info = info;
	}
    
}
')
