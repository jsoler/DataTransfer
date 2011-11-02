package com.emergya.aplicaciones.datatransfer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.springframework.stereotype.Service;


/**
 * Clase para transferir datos entre pojos y dtos
 * de forma automatica
 * 
 * @author jariera
 * @versio 1.0
 */
@Service
public class DataTransfer{
	
	
	private static final String GET_ID = "getId";

	private static final String SET_ID = "setId";

	private static final String PUNTO = ".";
	
	/** Formato de fecha por defecto */
	public static final String DATE_FORMAT = "dd/MM/yyyy";

	private static final String ESPACIO = " ";
	
	/** Formato de hora con segundos */
	public static final String TIME_SEC_FORMAT = "HH:mm:ss";
	
	
	
	/**
	 * devuelve un Pojo basico con tan solo el atributo Id relleno, este metodo es util para las relaciones
	 * básicas de los pojos
	 * @param dto
	 * @param pojoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object dtoToPojoBasic(Object dto, Class pojoClass) throws Exception {
		// Obtenemos la clase del pojo
		Class dtoClass = dto.getClass();
		// Instaciamos el objetoDto
		Object pojo = newInstance(pojoClass);
	
		Method [] metodos = dtoClass.getMethods();
		
		String metodoBuscado = GET_ID;
		String metodoSetBuscado = SET_ID;
				
		int indiceMetodo = haveMethod(metodoBuscado, metodos);
		int indiceMetodoSet = haveMethod(metodoSetBuscado, pojoClass.getMethods());
		
		if(indiceMetodo!=-1 && indiceMetodoSet!=-1){
			Object value = invokeGet(metodos, indiceMetodo, dto);
			Object [] args = {value};
			invoke(pojoClass, indiceMetodoSet, pojo, args);
		}
		
		return pojo;
	}
	
	/**
	 * devuelve un Pojo basico con tan solo el atributo Id relleno, este metodo es util para las relaciones
	 * básicas de los pojos
	 * @param dto
	 * @param pojoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object pojoToDtoBasic(Object pojo, Class dtoClass) throws Exception {
		// Obtenemos la clase del pojo
		Class pojoClass = pojo.getClass();
		// Instaciamos el objetoDto
		Object dto = newInstance(dtoClass);
	
		Method [] metodos = pojoClass.getMethods();
		
		String metodoBuscado = GET_ID;
		String metodoSetBuscado = SET_ID;
				
		int indiceMetodo = haveMethod(metodoBuscado, metodos);
		int indiceMetodoSet = haveMethod(metodoSetBuscado, dtoClass.getMethods());
		
		if(indiceMetodo!=-1 && indiceMetodoSet!=-1){
			Object value = invokeGet(metodos, indiceMetodo, pojo);
			Object [] args = {value};
			invoke(dtoClass, indiceMetodoSet, dto, args);
		}
		
		return dto;
	}
	
	/**
	 * pojo to dto simple
	 * @param dto
	 * @param pojoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object dtoToPojoSimple(Object dto, Class pojoClass) throws Exception {
		// Obtenemos la clase del pojo
		Class dtoClass = dto.getClass();
		// Instaciamos el objetoDto
		Object pojo = newInstance(pojoClass);
	
		Field [] atributos = dtoClass.getDeclaredFields();
		Method [] metodos = dtoClass.getMethods();
			
		for(int i=0; i<atributos.length; i++){
			Field atributo = atributos[i];
			String fieldname = atributo.getName();
			//construimos el nombre del Geter
			String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
			String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");
					
			int indiceMetodo = haveMethod(metodoBuscado, metodos);
			int indiceMetodoSet = haveMethod(metodoSetBuscado, pojoClass.getMethods());
			//Si es uno de los atributos que consideramos basicos en un dto lo copiamos
			if(indiceMetodo!=-1 && esTipoBasico(atributo.getType())){
				
				Object value = invokeGet(metodos, indiceMetodo, dto);
				
				if(indiceMetodoSet!=-1){
					
					if(atributo.getType().isAssignableFrom(Boolean.class)){
						int indBool = haveMethod(metodoSetBuscado + "Boolean", pojoClass.getMethods());
						if(indBool!=-1){
							indiceMetodoSet = indBool;
						}
					}
					Object [] args = {value};
					invoke(pojoClass, indiceMetodoSet, pojo, args);
				}
				
			}
		}
		return pojo;
	}
	
	

	
	

	
	/**
	 * pojo to dto simple
	 * @param dto
	 * @param pojoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object dtoToPojo(Object dto, Class pojoClass, Map atributosComplejos) throws Exception {
		
		Class dtoClass = dto.getClass();
		Object pojo = dtoToPojoSimple(dto, pojoClass);
	
		Method [] metodos = dtoClass.getMethods();
		
		Iterator itAtributos = atributosComplejos.entrySet().iterator();
		while(itAtributos.hasNext()){
			
			Entry entrada = (Entry)itAtributos.next();
			String fieldname = (String) entrada.getKey();
			//construimos el nombre del Geter
			String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
			String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");

			int indiceMetodo = haveMethod(metodoBuscado, metodos);
			int indiceMetodoSet = haveMethod(metodoSetBuscado, pojoClass.getMethods());
			
			//Solo recorreremos los atributos que tengan metodo set
			if(indiceMetodo!=-1){
				Object value = invokeGet(metodos, indiceMetodo, dto);

				if(indiceMetodoSet!=-1 && value != null){ 
					Object[] args = new Object[1];
					if(!esCollection(value)){
						args[0] = dtoToPojoSimple(value, (Class)entrada.getValue());
						//invoke(pojoClass, indiceMetodoSet, pojo, args);
					}else{
						Collection coleccion = (Collection) value;
						Collection destino = (Collection)newInstance(value.getClass());
						Iterator itCol = coleccion.iterator();
						while(itCol.hasNext()){
							Object valor = itCol.next();
							destino.add(dtoToPojoSimple(valor, (Class)entrada.getValue()));
						}

						args[0] = destino;
					}
					invoke(pojoClass, indiceMetodoSet, pojo, args);
				}
			}
		}
		
		return pojo;
	}
	
	

	
	/**
	 * Metodo que llama a las referencias mediante el metodo Basic que Obtiene solo el Id
	 * @param dto Objeto dto a convertir a Pojo
	 * @param pojoClass clase del Pojo al que hay que convertir
	 * @param atributosComplejos mapa de atributos complejos a convertir de forma básica
	 * @return
	 * @throws DataTranferException
	 */
	public static Object dtoToPojoConBasic(Object dto, Class pojoClass, Map atributosComplejos) throws Exception {
		Object pojo = pojoToDtoSimple(dto, pojoClass);
		if(atributosComplejos!=null){
			Class dtoClass = dto.getClass();
			
			//Field [] atributos = pojoClass.getDeclaredFields();
			Method [] metodos = dtoClass.getMethods();
			
			Iterator itAtributos = atributosComplejos.entrySet().iterator();
			while(itAtributos.hasNext()){
				
				Entry entrada = (Entry)itAtributos.next();
				String fieldname = (String) entrada.getKey();
				//construimos el nombre del Geter
				String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");
	
				int indiceMetodo = haveMethod(metodoBuscado, metodos);
				int indiceMetodoSet = haveMethod(metodoSetBuscado, pojoClass.getMethods());
				
				//Solo recorreremos los atributos que tengan metodo set
				if(indiceMetodo!=-1){
					Object value = invokeGet(metodos, indiceMetodo, dto);
					if(value!=null){
						if(indiceMetodoSet!=-1){
							Object[] args = new Object[1];
							if(!esCollection(value)){
								args[0] = dtoToPojoBasic(value, (Class)entrada.getValue());
								//invoke(dtoClass, indiceMetodoSet, dto, args);
							}else{
								Collection coleccion = (Collection) value;
								Collection destino = (Collection)newInstance(value.getClass());
								Iterator itCol = coleccion.iterator();
								while(itCol.hasNext()){
									Object valor = itCol.next();
									destino.add(dtoToPojoBasic(valor, (Class)entrada.getValue()));
								}
		
								args[0] = destino;
							}
							invoke(pojoClass, indiceMetodoSet, pojo, args);
						}
					}
				}
			}
		}
		
		return pojo;
	}
	
	/**
	 * 
	 * @param pojo
	 * @param dtoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object pojoToDto(Object pojo, Class dtoClass, Map atributosComplejos) throws Exception {
		
		//Si el pojo que que queremos pasar a DTO es nulo devolvemos null
		if(pojo==null){
			return null;
		}
		
		Object dto = pojoToDtoSimple(pojo, dtoClass);
		
		if(atributosComplejos!=null){
			Class pojoClass = pojo.getClass();
			
			//Field [] atributos = pojoClass.getDeclaredFields();
			Method [] metodos = pojoClass.getMethods();
			
			Iterator itAtributos = atributosComplejos.entrySet().iterator();
			while(itAtributos.hasNext()){
				
				Entry entrada = (Entry)itAtributos.next();
				String fieldname = (String) entrada.getKey();
				//construimos el nombre del Geter
				String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");
	
				int indiceMetodo = haveMethod(metodoBuscado, metodos);
				int indiceMetodoSet = haveMethod(metodoSetBuscado, dtoClass.getMethods());
				
				//Solo recorreremos los atributos que tengan metodo set
				if(indiceMetodo!=-1){
					Object value = invokeGet(metodos, indiceMetodo, pojo);
					if(indiceMetodoSet!=-1){
						Object[] args = new Object[1];
						args[0]=null;
						if(!esCollection(value)){
							args[0] = pojoToDtoSimple(value, (Class)entrada.getValue());
							//invoke(dtoClass, indiceMetodoSet, dto, args);
						}else{
							Collection coleccion = (Collection) value;
							Collection destino = (Collection)newInstance(value.getClass());
							Iterator itCol = coleccion.iterator();
							while(itCol.hasNext()){
								Object valor = itCol.next();
								destino.add(pojoToDtoSimple(valor, (Class)entrada.getValue()));
							}
	
							args[0] = destino;
						}
						invoke(dtoClass, indiceMetodoSet, dto, args);
					}
				}else{
					throw new Exception("Error No encontrado indiceMetodo para el atributo complejo: " + fieldname);
				}
			}
		}
		
		return dto;
	}
	
	/**
	 * 
	 * @param Metodo que llama a las referencias mediante el metodo Basic que Obtiene solo el Id
	 * @param dtoClass
	 * @return
	 * @throws DataTranferException
	 */
	public static Object pojoToDtoConBasic(Object pojo, Class dtoClass, Map atributosComplejos) throws Exception {
		Object dto = pojoToDtoSimple(pojo, dtoClass);
		if(atributosComplejos!=null){
			Class pojoClass = pojo.getClass();
			
			//Field [] atributos = pojoClass.getDeclaredFields();
			Method [] metodos = pojoClass.getMethods();
			
			Iterator itAtributos = atributosComplejos.entrySet().iterator();
			while(itAtributos.hasNext()){
				
				Entry entrada = (Entry)itAtributos.next();
				String fieldname = (String) entrada.getKey();
				//construimos el nombre del Geter
				String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
				String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");
	
				int indiceMetodo = haveMethod(metodoBuscado, metodos);
				int indiceMetodoSet = haveMethod(metodoSetBuscado, dtoClass.getMethods());
				
				//Solo recorreremos los atributos que tengan metodo set
				if(indiceMetodo!=-1){
					Object value = invokeGet(metodos, indiceMetodo, pojo);
					if(value!=null){
						if(indiceMetodoSet!=-1){
							Object[] args = new Object[1];
							if(!esCollection(value)){
								args[0] = pojoToDtoBasic(value, (Class)entrada.getValue());
								//invoke(dtoClass, indiceMetodoSet, dto, args);
							}else{
								Collection coleccion = (Collection) value;
								Collection destino = (Collection)newInstance(value.getClass());
								Iterator itCol = coleccion.iterator();
								while(itCol.hasNext()){
									Object valor = itCol.next();
									destino.add(pojoToDtoBasic(valor, (Class)entrada.getValue()));
								}
		
								args[0] = destino;
							}
							invoke(dtoClass, indiceMetodoSet, dto, args);
						}
					}
				}
			}
		}
		
		return dto;
	}
	
	/**
	 * metodo que invoca metodos sin argumentos
	 * @param metodos
	 * @param indiceMetodo
	 * @param invocado
	 * @return
	 * @throws DataTranferException
	 */
	private static Object invokeGet(Method [] metodos, int indiceMetodo, Object invocado) throws Exception{
		try {
			return metodos[indiceMetodo].invoke(invocado, null);
		} catch (IllegalArgumentException e1) {
			throw new Exception("Error en los argumentos al invocar el get de la clase" + invocado.getClass().getName(), e1);
		} catch (IllegalAccessException e1) {
			throw new Exception("Error al acceder al metodo get de la clase" + invocado.getClass().getName(), e1);
		} catch (InvocationTargetException e1) {
			throw new Exception( "Error al invocar la clase" + invocado.getClass().getName(), e1);
		}
	}
	
	/**
	 * metodo que invoca metodos
	 * @param clase
	 * @param indiceMetodo
	 * @param invocado
	 * @param args
	 * @return
	 * @throws DataTranferException
	 */
	private static Object invoke(Class clase, int indiceMetodo, Object invocado, Object[] args) throws Exception{
		try {
			return clase.getMethods()[indiceMetodo].invoke(invocado, args);
		} catch (IllegalArgumentException e) {
			throw new Exception("Error en los argumentos al invocar el set de la clase" + clase.getName(), e);
		} catch (SecurityException e) {
			throw new Exception("Error de seguridad al invocar el set de la clase" + clase.getName(), e);
		} catch (IllegalAccessException e) {
			throw new Exception("Error al acceder al set de la clase" + clase.getName(), e);
		} catch (InvocationTargetException e) {
			throw new Exception("Error al invocar la clase" + clase.getName(), e);
		}
	}
	
	/**
	 * metodo que crea nuevas instancias de la clase que se le pasen
	 * @param clase
	 * @return
	 * @throws DataTranferException
	 */
	private static Object newInstance(Class clase) throws Exception{
		try {
			return clase.newInstance();
		} catch (InstantiationException e1) {
			throw new Exception( "Error al instanciar la clase" + clase.getName(), e1);
		} catch (IllegalAccessException e1) {
			throw new Exception( "Error al instanciar la clase" + clase.getName(), e1);
		}
		
	}
	
	/**
	 * pojo to Dto
	 * @param pojo
	 * @param dtoClass
	 * @return dto converted to Pojo or null if pojo parameter is null
	 * @throws DataTranferException
	 */
	public static Object pojoToDtoSimple(Object pojo, Class dtoClass) throws Exception {
		
		//Si el pojo que que queremos pasar a DTO es nulo devolvemos null
		if(pojo==null){
			return null;
		}
		// Obtenemos la clase del pojo
		Class pojoClass = pojo.getClass();
		// Instaciamos el objetoDto
		Object dto = newInstance(dtoClass);
		
		//miramos los atributos del Dto
		Field [] atributosDto = dtoClass.getDeclaredFields();
		//almacenamos los metodos de acceso a los atributos
		Method [] metodos = pojoClass.getMethods();
		
		
		
		//recorremos los atributos para copiarlos
		for(int i=0; i<atributosDto.length; i++){
			Field atributo = atributosDto[i];
			String fieldname = atributo.getName();
			//construimos el nombre del Getter
			String metodoBuscado = "get"+ fieldname.replaceFirst(fieldname.substring(0, 1), fieldname.substring(0, 1).toUpperCase());
			String metodoSetBuscado = metodoBuscado.replaceFirst("g", "s");
			
			int indiceMetodo = haveMethod(metodoBuscado, metodos);
			int indiceMetodoSet = haveMethod(metodoSetBuscado, dtoClass.getMethods());
			//Si es uno de los atributos que consideramos basicos en un dto lo copiamos
			if(indiceMetodo!=-1 && esTipoBasico(atributo.getType())){
				
				Object value = null;
				//Si fuera un caracter booleano tenemos que tener cuidado al transferir el valor
				if(atributo.getType().isAssignableFrom(Boolean.class)){
					for(int e=0; e<metodos.length;e++){
						if(metodos[e].getName().equals(metodoBuscado + "Boolean")){
							/*
							System.out.println("es booelan!!");
							*/
							value = invokeGet(metodos, e, pojo);
							break;
						}
					}
				}else{
					value = invokeGet(metodos, indiceMetodo, pojo);
				}
				
				if(indiceMetodoSet!=-1){
					/*
					System.out.println("tiene Set!!!");
					*/
					Object [] args = {value};
					invoke(dtoClass, indiceMetodoSet, dto, args);
				}
				
			}
		}
		return dto;
	}
	
	/**
	 * Comprueba la existencia de un metodo en un array de metodos, devolviendo
	 * la posicion del metodo en el array, si el metodo buscado no se encuentra dentro
	 * la función devuelve -1
	 * @param methodName
	 * @param metodos
	 * @return
	 */
	private static int haveMethod(String methodName, Method[] metodos){
		
		int tiene = -1;
		for(int i=0; i<metodos.length; i++){
			Method metodo = metodos[i];
			if(metodo.getName().equals(methodName)){
				tiene=i;
				break;
			}
		}
		
		return tiene;
	}

	/**
	 * Metodo que comprueba si una clase es un tipo basico de mapeo
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo basico o false en caso contrario
	 */
	public static boolean esTipoBasico(Class c) {
		return (c.isAssignableFrom(Long.class) || c.isAssignableFrom(Double.class) || c.isAssignableFrom(String.class)
				|| c.isAssignableFrom(Boolean.class) || c.isAssignableFrom(Calendar.class)
				|| c.isAssignableFrom(Character.class) || c.isAssignableFrom(Byte.class)
				|| c.isAssignableFrom(Short.class)
				|| c.isAssignableFrom(Integer.class)
				|| c.isAssignableFrom(GregorianCalendar.class)
				|| c.isAssignableFrom(Date.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo basico de mapeo
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo basico o false en caso contrario
	 */
	public static boolean esCollection(Object c) {
		return c instanceof Collection;
		//return (c.isInstance(Collection.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Date
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Date
	 */
	public static boolean esDate(Class c) {
		return (c.isAssignableFrom(Date.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Calendar
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Calendar
	 */
	public static boolean esCalendar(Class c) {
		return (c.isAssignableFrom(Calendar.class) 
				|| c.isAssignableFrom(GregorianCalendar.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Long
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Long
	 */
	public static boolean esLong(Class c) {
		return (c.isAssignableFrom(Long.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Integer
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Integer
	 */
	public static boolean esInteger(Class c) {
		return (c.isAssignableFrom(Integer.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Double
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Double
	 */
	public static boolean esDouble(Class c) {
		return (c.isAssignableFrom(Double.class));
	}

	/**
	 * Metodo que comprueba si una clase es un tipo Boolean
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Boolean
	 */
	public static boolean esBoolean(Class c) {
		return (c.isAssignableFrom(Boolean.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Float
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Float
	 */
	public static boolean esFloat(Class c) {
		return (c.isAssignableFrom(Float.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo String
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo String
	 */
	public static boolean esString(Class c) {
		return (c.isAssignableFrom(String.class));
	}
	
	/**
	 * Metodo que comprueba si una clase es un tipo Short
	 * @param c Clase a comprobar
	 * @return boolean true si es un tipo Short
	 */
	public static boolean esShort(Class c) {
		return (c.isAssignableFrom(Short.class));
	}
	
	/**
	 * Metodo que devuelve el atributo nombreAtributo
	 * de atributo 
	 * 
	 * @param nombreAtributo nombre del atributo que queremos obtener, puede ser del modo "subAt.subSubAt..."
	 * @param atributo obteto del que queremos obtener el atributo
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static Object obtenerAtributo(String nombreAtributo, Object atributo) 
			throws IllegalArgumentException, IllegalAccessException{
	
			StringTokenizer st = new StringTokenizer(nombreAtributo,PUNTO);
			java.lang.reflect.Field[] campos = atributo.getClass().getDeclaredFields();
			Field.setAccessible(campos, true);
			if(st == null || st.countTokens()<2){
				
				for (int i = 0; i<campos.length; i++){
					Field campo = campos[i];
					if (campo.getName().equals(nombreAtributo)){
							return campo.get(atributo);
					}
				}
			}else{
				//Caso recursivo
				String primero = (String) st.nextElement();
				for (int i = 0; i<campos.length; i++){
					Field campo = campos[i];
					if (campo.getName().equals(primero)){
						StringBuffer atributosRestantes = new StringBuffer("");
						while(st.hasMoreTokens()){
							atributosRestantes.append(st.nextToken());
							if(st.hasMoreTokens()){
								atributosRestantes.append(PUNTO);
							}
						}
						//Llamada recursiva
						return obtenerAtributo(atributosRestantes.toString(), campo.get(atributo));
					}
				}
			}
		
		return null;
	}
	
	/**
	 * Metodo que devuelve el atributo nombreAtributo
	 * de obj. Util para forzar la carga perezosa asociada a los GETTERS
	 * 
	 * @param nombreAtributo nombre del atributo que queremos obtener, puede ser del modo "subAt.subSubAt..."
	 * @param obj obteto del que queremos obtener el atributo
	 * 
	 * @return valor del atributo o null si obj es null o no encuentra el metodo
	 * 
	 * @throws DataTranferException 
	 */
	public static Object obtenerAtributoConGet(String nombreAtributo, Object obj) throws Exception{

		Object value = null;
		
		if(obj != null){
			//almacenamos los metodos de acceso a los atributos
			Method [] metodos = obj.getClass().getMethods();
			
			String metodoBuscado = "get"+ nombreAtributo.replaceFirst(nombreAtributo.substring(0, 1), nombreAtributo.substring(0, 1).toUpperCase());
	
			int indiceMetodo = haveMethod(metodoBuscado, metodos);
			
			if(indiceMetodo!=-1){
				value = invokeGet(metodos, indiceMetodo, obj);
			}
		}
		
		return value;
	}
	
	/**
	 * Metodo que establece el atributo nombreAtributo
	 * de dto
	 * 
	 * @param nombreAtributo nombre del atributo que queremos obtener, puede ser del modo "subAt.subSubAt..."
	 * @param dto objeto al que le queremos establecer el atributo
	 * @param value valor que se quiere establecer
	 * 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void establecerAtributo(String nombreAtributo, Object dto, Object value) 
			throws IllegalArgumentException, IllegalAccessException{
	
			StringTokenizer st = new StringTokenizer(nombreAtributo,PUNTO);
			java.lang.reflect.Field[] campos = dto.getClass().getDeclaredFields();
			Field.setAccessible(campos, true);
			if(st == null || st.countTokens()<2){
				
				for (int i = 0; i<campos.length; i++){
					Field campo = campos[i];
					if (campo.getName().equals(nombreAtributo)){
						//Caso base
						campo.set(dto, value);
					}
				}
			}else{
				//Caso recursivo
				String primero = (String) st.nextElement();
				for (int i = 0; i<campos.length; i++){
					Field campo = campos[i];
					if (campo.getName().equals(primero)){
						StringBuffer atributosRestantes = new StringBuffer("");
						while(st.hasMoreTokens()){
							atributosRestantes.append(st.nextToken());
							if(st.hasMoreTokens()){
								atributosRestantes.append(PUNTO);
							}
						}
						//Llamada recursiva
						establecerAtributo(atributosRestantes.toString(), campo.get(dto), value);
					}
				}
			}
	}
	
	/**
	 * Metodo que convierte a String diferentes objetos de tipo basico: 
	 * Boolean, Integer, Double, Long, Float, Date y Calendar, 
	 * si el tipo del objeto no correponde con ninguno de los anteriores simplemente se llamara a 
	 * su metodo toString para la conversion
	 * @param item objeto a convertir
	 * @param dateFormat formato al que convertir los tipos Date y Calendar, si es nulo se usa un formato por defecto 
	 * @return el valor del objeto convertido a String
	 */
	public static String aString(Object item, String dateFormat){
		String format = formatDateNotNull(dateFormat);
		String valor = null;
		if(item != null){
			if(esBoolean(item.getClass())){
				valor = ((Boolean)item).toString();
			} else if(esDate(item.getClass())){
				valor = VenezuelacertUtil.transformDate((Date)item, format);
			} else if(esCalendar(item.getClass())){
				valor = VenezuelacertUtil.transformCalendar((Calendar)item, format);
			} else {
				valor = item.toString();
			}
		}
		
		return valor;
	}
	
	/**
	 * metodo de formateo de fechas
	 * @param dateFormat
	 * @return
	 */
	private static String formatDateNotNull(String dateFormat) {
		String format = dateFormat;
		if(dateFormat == null || "".equals(dateFormat)){
			format = DATE_FORMAT + ESPACIO + TIME_SEC_FORMAT;
		}
		return format;
	}
	


	/**
	 * Metodo que convierte una lista de objetos en una matriz de cadenas con los valores de
	 *  los atributos que queramos recuperar 
	 * de dichos objetos
	 * @param propiedades Array de String con los nombres de los atributos que queremos recuperar
	 * @param listado Lista de Objetos para recuperar valores
	 * @param dateFormat a traves de este parametro podremos definir un formato de 
	 * presentacion para los objetos de tipo Calendar y Date
	 * si dejamos este valor a null se aplica Formato por defecto: dd/MM/yyyy HH:mm:ss
	 * @return String [][] Array bidimensional o tabla con todos los atributos convertidos a String
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static String [][] transformaDatosString(String[] propiedades, List listado, String dateFormat) 
		throws IllegalArgumentException, IllegalAccessException{
		String [][] result = null;
		if(propiedades!=null && listado!=null){
			result = new String[listado.size()][propiedades.length];//Array donde guardaremos la celda de valores
			int index = 0;
			
			//Recorremos La lista de Objetos
			Iterator it = listado.iterator();
			while (it.hasNext()){
				Object item = it.next();
				for(int colIndex = 0; colIndex < propiedades.length; colIndex++){
					//navegamos a traves de la jerarquia de propiedades y recuperamos el valor especificado
					Object propiedad = obtenerAtributo(propiedades[colIndex], item);
					result[index][colIndex] = aString(propiedad, dateFormat);
				}
				index++;
			}
		}
		return result;
	}

	
	
	
	

}
