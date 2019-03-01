package com.redhat.customer.translate;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;

import com.customer.app.Person;
import com.customer.app.PersonName;
import com.sun.mdm.index.webservice.CallerInfo;
import com.sun.mdm.index.webservice.ExecuteMatchUpdate;
import com.sun.mdm.index.webservice.PersonBean;
import com.sun.mdm.index.webservice.SystemPerson;

@Converter
public class TransformToExecuteMatch {

  @Converter
  public ExecuteMatchUpdate convertTo(Object value, Exchange exchange)
  throws TypeConversionException {

    Person person = (Person)value;
    SystemPerson systemPerson = new SystemPerson();
    PersonBean personBean = new PersonBean();

    // we only set the Father's name and Gender
    // Any of the other person objects could be set here
    if(person.getLegalname() != null) {
    	if(person.getLegalname().getGiven() != null)
    		personBean.setFirstName(person.getLegalname().getGiven());
    }
    if(person.getFathername()!=null)
    	personBean.setFatherName(person.getFathername());
    if(person.getGender() != null) {
    	if(person.getGender().getCode() != null)
    		personBean.setGender(person.getGender().getCode());
    }
    	
    if(person.getIdentifier()!= null) {
    	if(person.getIdentifier().getIdentifier()!= null)
    		personBean.setPersonId(person.getIdentifier().getIdentifier());
    }
    if(person.getBirthname()!= null) {
    	personBean.setFirstName(person.getBirthname());
    }
    	
    systemPerson.setPerson(personBean);

    // These only show up in the logs and can be anything
    // We set the user to Xlate here to know the ESB was used
    CallerInfo callerInfo = new CallerInfo();
    callerInfo.setApplication("App");
    callerInfo.setApplicationFunction("Function");
    callerInfo.setAuthUser("Xlate");

    ExecuteMatchUpdate executeMatchUpdate = new ExecuteMatchUpdate();
    executeMatchUpdate.setCallerInfo(callerInfo);
    executeMatchUpdate.setSysObjBean(systemPerson);

    if(exchange!=null ){
      exchange.getOut().setBody(executeMatchUpdate);
    }

    return executeMatchUpdate;
  }

}
