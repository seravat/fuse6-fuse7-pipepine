package com.redhat.usecase.service.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import com.customer.app.ID;
import com.customer.app.Person;
import com.customer.app.response.ESBResponse;
import com.redhat.usecase.service.DEIMService;


public class DEIMServiceImpl implements DEIMService {

  @Produce(uri = "direct:integrateRoute")
  ProducerTemplate template;
  
  @Override
  @POST
  @Path("/persons")
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_XML)
  public Response addPerson(Person person) {

    ResponseBuilderImpl builder = new ResponseBuilderImpl();

    // This header is used to direct the message in the Camel route
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("METHOD", "add");

    try {
      String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(),
      person, headers, String.class);

      ESBResponse esbResponse = new ESBResponse();
      esbResponse.setBusinessKey(UUID.randomUUID().toString());
      esbResponse.setPublished(true);

      // Here we hard code the response code values to strings for the demo
      // A better practice would be to have an ENUM class

      String personXMLString = marshalPerson(person);
      
      String comment = "NONE";
      if (camelResponse != null && !camelResponse.equals(personXMLString)) {
        comment = "NOT ADDED";
      } else if (camelResponse != null && camelResponse.equals(personXMLString)) {
        comment = "ADDED";
      } else {
        comment = "ERROR";
      }
      esbResponse.setComment(comment);

      builder.status(Response.Status.CREATED);
      builder.entity(esbResponse);

    } catch (CamelExecutionException cee) {
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
      builder.entity(cee.getMessage());
    }catch(Exception e){
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
      builder.entity(e.getMessage());
      e.printStackTrace();
    }

    return builder.build();
  }
  
  @Override
  @PUT
  @Path("/persons")
  @Consumes(MediaType.APPLICATION_XML)
  public Response updatePerson(Person person) {

    ResponseBuilderImpl builder = new ResponseBuilderImpl();

    // This header is used to direct the message in the Camel route
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("METHOD", "update");

    try {
      String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(),
      person, headers, String.class);

      ESBResponse esbResponse = new ESBResponse();
      esbResponse.setBusinessKey(UUID.randomUUID().toString());
      esbResponse.setPublished(true);

      // Here we hard code the response code values to strings for the demo
      // A better practice would be to have an ENUM class
      String personXMLString = marshalPerson(person);
      
      String comment = "NONE";
      if (camelResponse != null && !camelResponse.equals(personXMLString)) {
        comment = "NO MATCH";
      } else if (camelResponse != null && camelResponse.equals(personXMLString)) {
        comment = "UPDATED";
      } else {
        comment = "ERROR";
      }
      esbResponse.setComment(comment);

      builder.status(Response.Status.OK);
      builder.entity(esbResponse);

    } catch (CamelExecutionException cee) {
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
      builder.entity(cee.getMessage());
    }catch(Exception e){
      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
      builder.entity(e.getMessage());
      e.printStackTrace();
    }

    return builder.build();
  }
  
  @Override
  @GET
  @Path("/persons/{name}")
  public Response searchPerson(@PathParam("name") String name) {

    ResponseBuilderImpl builder = new ResponseBuilderImpl();

    // This header is used to direct the message in the Camel route
    Map<String, Object> headers = new HashMap<String, Object>();
    headers.put("METHOD", "search");
    
    try {
    	 
    	  if(name == null) {
    		  return builder.status(Status.BAD_REQUEST).build(); 
    	  }
    	  
		  Person person = new Person();
		  person.setBirthname(name);   
      
	      String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(),
	      person, headers, String.class);
	
	      ESBResponse esbResponse = new ESBResponse();
	      esbResponse.setBusinessKey(UUID.randomUUID().toString());
	      esbResponse.setPublished(true);
	
	      // Here we hard code the response code values to strings for the demo
	      // A better practice would be to have an ENUM class
	      //String personXMLString = marshalPerson(person);
	      
	      String comment = "NONE";
/*	      if (camelResponse != null && !camelResponse.equals(personXMLString)) {
	        comment = "NO MATCH";
	      } else if (camelResponse != null && camelResponse.equals(personXMLString)) {
	        comment = "MATCH";
	      } else {
	        comment = "ERROR";
	      }*/
	      esbResponse.setComment(comment);
	
	      builder.status(Response.Status.OK);
	      builder.entity(esbResponse);

    	} catch (CamelExecutionException cee) {
	      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
	      builder.entity(cee.getMessage());
	    }catch(Exception e){
	      builder.status(Response.Status.INTERNAL_SERVER_ERROR);
	      builder.entity(e.getMessage());
	      e.printStackTrace();
	    }

    return builder.build();
  }
  
  public String marshalPerson(Person person) {
	try {
		String result;
		JAXBContext jaxbContext= JAXBContext.newInstance(Person.class);
		Marshaller marshaller=jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(person, sw);

		result = sw.toString();
	    return result;
	} catch (JAXBException e) {
		e.printStackTrace();
		return null;
	}
  }


}
