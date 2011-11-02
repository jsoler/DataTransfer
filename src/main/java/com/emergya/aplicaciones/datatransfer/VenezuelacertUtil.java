package com.emergya.aplicaciones.datatransfer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Clase de utilidades para Venezuelacert
 * @author jariera
 * @version 1.0
 *
 */
public class VenezuelacertUtil{
	
	/**Formato para fechas */
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
    /**
     * Constante que define el formato unicode que se emplea.
     */
    private static final String UNICODE_FORMAT = "UTF8";

    /**
     * Constante para el calculo de la letra asociada a un DNI y que 
     * permite la obtencion del NIF.
     */
    private static final String NIF_STRING_ASOCIATION = "TRWAGMYFPDXBNJZSQVHLCKET";

    /**
     * Conjunto de valores hexadecimales utilizados para la conversion de 
     * bytes a cadenas hexadecimales.
     */
    private static final char[] HexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    
    /** TILDES */
    private static final char[] TILDES = {'á', 'é', 'í', 'ó', 'ú', 'Á', 'É', 'Í', 'Ó', 'Ú'};
	
	private static final char[] NO_TILDES = {'a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U'};

    /**
     * Constructor.
     * 
     * @throws Exception excepcion producida.
     */
    public VenezuelacertUtil() {
      
    }

    /**
     * Método que obtiene un String a partir de un array de bytes.
     * 
     * @param bytes array de bytes.
     * @return Cadena que contiene al array de bytes.
     */
    public static String bytes2String(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        char c;
        for (int i = 0; i < bytes.length; i++) {
            // Java no tiene unsigned bytes, pero los char si son unsigned.
            if (bytes[i] < 0) {
                c = (char)(bytes[i] + 256);
            }
            else {
                c = (char)bytes[i];
            }
            stringBuffer.append(c);
        }

        return stringBuffer.toString();
    }

    /**
     * Convierte un array de bytes a cadena Hexadecimal.
     * 
     * @param bytes array de bytes a convertir.
     * @return String cadena en hexadecimal.
     */
    public static final String toHexString(byte[] bytes) {

        StringBuffer sb = new StringBuffer();


        for (int i = 0; i < bytes.length; i++) {

            sb.append(HexChars[(bytes[i] >> 4) & 0xf]);
            sb.append(HexChars[bytes[i] & 0xf]);
        }

        return sb.toString();
    }

    /**
     * Método que valida una estructura XML mediante el Schema correspondiente,
     * no lanzando ninguna excepcion en caso de que se realice correctamente.
     * 
     * @param xml estructura XML.
     * @param rutaXsd URL del schema de validacion.
     * @throws SAXException excepcion de parseo.
     * @throws IOException excepcion de lectura/escritura.
     */
    public static void validateXMLwithXSD(String xml, String rutaXsd) throws SAXException, IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL urlLoader = loader.getResource(rutaXsd);
        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Schema schema = factory.newSchema(urlLoader);

        Validator validator = schema.newValidator();

        Source source = new StreamSource(new StringReader(xml));
        validator.validate(source);
    }

    /**
     * Verifica el formato y valor del NIE.
     * Debe ser X09999999A, siendo X la letra X y A la letra que corresponda.
     *
     * @param sNie cadena con el NIE.
     * @return codigo de error si lo hubiera.
     * @throws NullPointerException si el NIE introducido es nulo.
     * @throws NumberFormatException si el NIE introducido no tiene una secuencia
     * de numeros valida tras el primer caracter obligatorio 'X'.
     * @thorws Exception si el tamano o secuencia de numeros no corresponden con 
     * un NIE valido.
     */
    public static boolean checkNIE(String sNie) throws NullPointerException, NumberFormatException, Exception {
        int iNie;
        char cLetra;
        char cLetraBuena;

        boolean isNIE = false;

        if (sNie == null) {
            throw new NullPointerException("El NIE introducido tiene valor nulo.");

        }

        else if (sNie.length() != 10) {
            throw new Exception("Tamano de NIE incorrecto");

        }

        else {

            if (sNie.charAt(0) != 'X') {
                throw new Exception("Formato de NIE incorrecto: Debe comenzar por el caracter X");
            }
            else {
                try {
                    iNie = Integer.parseInt(sNie.substring(1, 9));
                }
                catch (NumberFormatException e) {
                    throw new NumberFormatException("Formato de NIE incorrecto.");
                }
                cLetra = sNie.charAt(9);
                cLetraBuena = NIF_STRING_ASOCIATION.charAt(iNie % 23);
                if (cLetra != cLetraBuena) {
                    throw new Exception("Formato de NIE incorrecto: " + "La letra final del NIE no es correcta.");
                }
                else {
                    isNIE = true;
                }
            }
        }

        return isNIE;
    }

    /**
     * Verifica el formato y valor del DNI.
     *
     * @param sDni cadena con el DNI.
     * @return codigo de error si lo hubiera, null si es correcto.
     * @throws NullPointerException si el DNI introducido es nulo.
     * @throws NumberFormatException si el DNI introducido no tiene una secuencia
     * de numeros valida.
     * @throws Exception si el tamano o secuencia de numeros no corresponden con 
     * un NIE valido.
     */
    public static boolean checkDNI(String sDni) throws NullPointerException, NumberFormatException, Exception {
        int iDni;
        char letraBuena;
        char letra;
        boolean isDNI = false;

        if (sDni == null) {
            throw new NullPointerException("El DNI introducido tiene valor nulo.");
        }
        else if (sDni.length() != 10) {
            throw new Exception("Tamano de DNI incorrecto");
        }
        else {
            try {
                iDni = Integer.parseInt(sDni.substring(0, 9));
            }
            catch (NumberFormatException e) {
                throw new NumberFormatException("Formato de DNI incorrecto. Debe " + 
                                                "contener una secuenca numerica valida.");
            }
            letra = sDni.charAt(9);
            letraBuena = NIF_STRING_ASOCIATION.charAt(iDni % 23);
            if (letra != letraBuena) {
                throw new Exception("Formato de DNI incorrecto: La letra final " + "del NIF no es correcta.");
            }
            else {
                isDNI = true;
            }
        }
        return isDNI;
    }

    /**
     * Método que comprueba si una cadena puede tomarse como numero Long.
     * 
     * @param value cadena.

     * @return true si se puede tomar como numero, false en caso contrario.
     */
    public static boolean isLong(String value) {
        try {
            Long.parseLong(value);

            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Método que devuelve en mayusculas y sin acentos la cadena pasada como parametro.
     * 
     * @param valor cadena original.
     * @return cadena en mayusculas y sin acentos.
     */
    public static String sValor(String valor) {
        String devolver = null;

        if ((valor != null) && (!"".equals(valor))) {
            devolver = valor.toUpperCase();
            devolver = devolver.replace("Á", "A");
            devolver = devolver.replace("À", "A");
            devolver = devolver.replace("Ä", "A");
            devolver = devolver.replace("Ã", "A");
            devolver = devolver.replace("Â", "A");
            devolver = devolver.replace("É", "E");
            devolver = devolver.replace("È", "E");
            devolver = devolver.replace("Ë", "E");
            devolver = devolver.replace("Ê", "E");
            devolver = devolver.replace("Í", "I");
            devolver = devolver.replace("Ì", "I");
            devolver = devolver.replace("Ï", "I");
            devolver = devolver.replace("Î", "I");
            devolver = devolver.replace("Ó", "O");
            devolver = devolver.replace("Ò", "O");
            devolver = devolver.replace("Ö", "O");
            devolver = devolver.replace("Õ", "O");
            devolver = devolver.replace("Ô", "O");
            devolver = devolver.replace("Ú", "U");
            devolver = devolver.replace("Ù", "U");
            devolver = devolver.replace("Ü", "U");
            devolver = devolver.replace("Û", "U");
        }
        return devolver;
    }

    /**
     * Método que verifica el formato de un NIE.
     * 
     * @param sNie nie a verificar.
     * @return Cadena con el texto de error.
     */
    public static String verificarNIE(String sNie) {
        final String FORMATO_NIE_INCORRECTO = "Formato del NIE incorrecto.";
        final String NIE_NULO = "Debe informarse el NIE.";
        int iNie;
        char cLetra;
        char cLetraBuena;
        String devolver = null;

        try {
            if (sNie == null) {
                devolver = NIE_NULO;
            }
            else if (sNie.length() != 10) {
                devolver = FORMATO_NIE_INCORRECTO;
            }
            else {
                if (sNie.charAt(0) != 'X') {
                    devolver = FORMATO_NIE_INCORRECTO;
                }
                else {
                    iNie = Integer.parseInt(sNie.substring(1, 9));
                    cLetra = sNie.charAt(9);
                    cLetraBuena = NIF_STRING_ASOCIATION.charAt(iNie % 23);

                    if (cLetra != cLetraBuena) {
                        devolver = FORMATO_NIE_INCORRECTO;
                    }
                }

            }
        }
        catch (Exception e) {

            devolver = FORMATO_NIE_INCORRECTO;
        }
        return devolver;
    }

    /**
     * Método que verifica el formato de un DNI.
     * 
     * @param sDni dni a verificar.
     * @return Cadena con el texto de error.
     */
    public static String verificarDNI(String sDni) {
        final String FORMATO_DNI_INCORRECTO = "Formato del DNI incorrecto.";
        final String DNI_NULO = "Debe informarse el DNI.";
        int iDni;
        char letraBuena;
        char letra;
        String devolver = null;

        try {
            if (sDni == null) {
                devolver = DNI_NULO;

            }

            else if (sDni.length() != 9) {
                devolver = FORMATO_DNI_INCORRECTO;
            }
            else {
                iDni = Integer.parseInt(sDni.substring(0, 8));
                letra = sDni.charAt(8);
                letraBuena = NIF_STRING_ASOCIATION.charAt(iDni % 23);

                if (letra != letraBuena) {
                    devolver = FORMATO_DNI_INCORRECTO;
                }
            }
        }
        catch (Exception e) {
            devolver = FORMATO_DNI_INCORRECTO;
        }
        return devolver;
    }

    /**
     * Comprueba si una cadena es nula, vacia o contiene solo espacios
     * 
     * @param cadena Cadena que quiere ser verificada
     * 
     * @return True si la cadena de entrada es vacia, nula o solo contiene espacios
     */
    public static boolean isEmpty(String cadena) {
        boolean result = false;

        try {
            if (cadena == null) {
                result = true;
            }
            else if (cadena.trim().equals("")) {
                result = true;
            }
        }
        catch (Exception e) {
            result = true;
        }

        return result;
    }

    /**
     * Método que lee un fichero a un String
     * 
     * @param filePath ruta al fichero.
     */
    public static String readFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
    
    /**
     * Método que devuelve la cadena de entrada con el encoding que se le pasa
     * como parametro.
     * 
     * @param cadenaEntrada
     * @param charsetName
     * @return cadena de entrada con el encoding que se le pasa
     * @throws Exception
     */
    public static String encodeToCharset(String cadenaEntrada, String charsetName) throws Exception {
        String cadenaResult = null;
        
        if (cadenaEntrada != null) {
            cadenaResult = new String(cadenaEntrada.getBytes(charsetName));        
        }
        
        return cadenaResult;
    }

    /**
     * Método que obtiene el stream asociado a la cadena.
     * 
     * @param s cadena.
     * @return stream asociado a la cadena.
     */
    public static InputStream stringToInputStream(String s) {

        InputStream stream = null;

        stream = new ByteArrayInputStream(s.getBytes());

        return stream;
    }
    
    /**
     * Método que devuelve un fichero como un array de bytes
     * 
     * @param filePath
     * @return
     * @throws Exception
     */
    public static byte [] readFileAsByteArray(String filePath) throws Exception {
        byte[] fileBytes = null;
        File fileInforme = new File(filePath);
        if (fileInforme.isFile()) {            
            FileInputStream fileInput = new FileInputStream(fileInforme);
            int available = fileInput.available();
            fileBytes = new byte[available];
            fileInput.read(fileBytes);
        }
        return fileBytes;
    }
    
    /**
     * Método que convierte un BigDecimal a Long
     * 
     * @param decimal
     * 
     * @return Long convertido desde un BigDecimal
     */
    public static Long convertBigDecimalToLong(BigDecimal decimal) {
        Long result = null;

        try {
            if (decimal != null) {
                result = new Long(decimal.longValue());
            }
        }
        catch (Exception e) {
            result = null;
        }

        return result;    

    }  
    
    
    
    

	/**
	 * Metodo que convierte un Date a String en un
	 * determinado formato
	 * 
	 * @param date Date que representa la fecha
	 * @param format String que tiene el formato
	 * @return String
	 */
	public static String calendarToString(Calendar c, String format) {
		if(c == null)
			return null;
		else
			return transformDate(c.getTime(), format);
	}

	/**
	 * Metodo que convierte un Date a String en un
	 * determinado formato
	 * 
	 * @param date
	 * @param format
	 * @return String
	 */
	
	public static String transformDate(Date date, String format) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat sdfOutput = new SimpleDateFormat(format);

		return sdfOutput.format(date);
	}
	
	
	/**
	 * Metodo que convierte un Calendar en un determinado formato
	 * a String
	 * 
	 * @param c Calendar que representa la fecha
	 * @param format String que tiene el formato con el que esta
	 *            representada la fecha
	 * @return String con la fecha de c o null si
	 *         hay algun error
	 */
	public static String transformCalendar(Calendar c, String format) {
		if(c == null) {
			return null;
		} else {			
			return transformDate(c.getTime(), format);
		}
	}
	
	/**
	 * Metodo que devuelve la fecha actual
	 * 
	 * @return Calendar Fecha Actual
	 */
	public static Calendar currentDate() {
		Calendar c = new GregorianCalendar();
		
		c.setTimeInMillis(System.currentTimeMillis());
		
		return c;
	}
	

	/**
	 * Metodo que convierte un String en un determinado formato a
	 * Calendar
	 * 
	 * @param valor String que representa la fecha
	 * @param format String que tiene el formato con el que esta
	 *            representada la fecha
	 * @return Calendar con la fecha de valor o
	 *         null si hay algun error
	 */
	public static Calendar transformCalendar(String valor, String format) {
		Calendar c = new GregorianCalendar();
		try {
			c.setTime(transformDate(valor, format));
		}catch(Exception e) {
			return null;
		}
		return c;
	}

	/**
	 * Metodo que convierte un String en un determinado formato a
	 * Date
	 * 
	 * @param value String que representa la fecha
	 * @param format String que tiene el formato con el que esta
	 *            representada la fecha
	 * @return Date con la fecha de valor o null
	 *         si hay algun error
	 */
	public static Date transformDate(String value, String format) {
		if(value == null || value.length() == 0) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = sdf.parse(value);
		}catch(Exception e) {
			
		}
		return d;
	}
	
	/**
     * Obtiene el Date de un Calendar del mismo que lo hace Torque<br/>
     * Copiado de <a href="http://db.apache.org/torque/releases/torque-3.3/village/apidocs/com/workingdogs/village/Value.html">com.workingdogs.village.Value#asUtilDate</a>  
     *
     * @return a java.util.Date
     *
     * @exception DataSetException
     * 
     * @see {@link com.workingdogs.village.Value#asUtilDate()}
     * @see <a href="http://db.apache.org/torque/releases/torque-3.3/village/apidocs/com/workingdogs/village/Value.html">API torque</a>
     * @see <a href="http://svn.apache.org/repos/asf/db/torque/village/trunk/src/java/com/workingdogs/village/Value.java">Source code</a>
     */
    public static java.util.Date asUtilDate(Calendar cal)
    {
    	return java.sql.Date.valueOf(cal.get(Calendar.YEAR) + "-"
                + leadingZero(cal.get(Calendar.MONTH) + 1) + "-"
                + leadingZero(cal.get(Calendar.DAY_OF_MONTH)));
    }
    
    /**
     * Convert an int to a two digit String with a leading zero where necessary.
     * Copiado de com.workingdogs.village.Value#leadingZero(int)
     *
     * @param val The value to be converted.
     * @return A two character String with leading zero.
     * 
     * @see com.workingdogs.village.Value#leadingZero(int)
     * @see http://svn.apache.org/repos/asf/db/torque/village/trunk/src/java/com/workingdogs/village/Value.java
     */
    private static String leadingZero(int val)
    {
        return (val < 10 ? "0" : "") + val;
    }


	
	/**
	 * Transforma un Calendar a Date
	 * @param c
	 * @return
	 */
	public static Date calendarToDate(Calendar c){
		return transformDate(calendarToString(c, DATE_FORMAT), DATE_FORMAT);
	}
	
	/**
	 * Crea una nueva cadena similar a la primera pero cambiando las vocales con tilde
	 * por la correspondiente sin ella.
	 * @param cadena
	 * @return
	 */
	public static String transformaCadena(String cadena){
		
		String sinTilde = cadena; //para que la cadena de entrada no quede modificada
		
		for(int i=0; i<TILDES.length;i++){
			sinTilde = sinTilde.replace(TILDES[i], NO_TILDES[i]);
		}
		return sinTilde;
	}
	
	
	/**
	 * Meodo que devuelve un long a cadena, si este es null retorna la cadena vacia
	 * ""
	 * @param var Long a convertir
	 * @return Long convertido a cadena
	 */
	public static String longToString(Long var){
		if(var != null){
			return var.toString();
		}else{
			return "";
		}
	}
	
	/**
	 * Metodo que convierte un Short a un Integer
	 * @param obj
	 * @return
	 */
	public static Integer ShortToInteger (Short obj){
		if(obj != null){
			return new Integer(obj.intValue());
		}
		return null;
	}

	/**
	 * Convierte un array de Strings a array de Longs 
	 * 
	 * @param ids
	 * 
	 * @return array con los Longs resultantes
	 */
	public static Long[] convertirStringsALongs(String[] ids) throws Exception{
		Long [] resultado = null;
		try{
			if(ids != null){
				resultado = new Long[ids.length];
				 if(ids != null && ids.length > 0){
			    	for (int i = 0; i < ids.length; i++){
			    		resultado[i] = Long.decode(ids[i]);
			    	}
				 }
			}
		}catch (Exception e){
			throw new Exception();
		}
		return resultado;
	}

	/**
	 * @param fecha
	 * @param format
	 * @return
	 */
	public static String parse(String fecha, String format) {
		return transformCalendar(fecha, format).toString();
	}
	
	/**
	 * Convierte la lista de long a una lista de strings
	 * 
	 * @param listaLong
	 * @return
	 * @throws Exception
	 */
	public static List<String> listLongToListString(List<Long> listaLong) throws Exception{
		try{
			List<String> listaString = new LinkedList<String>();
			for(Long id: listaLong){
				listaString.add(id.toString());
			}
			return listaString;
		}catch (Exception e){
			throw new Exception();
		}
	}
	
	
	/**
	 * Indica si el numero es mayor que 0
	 * @param dato
	 * @return
	 */
	public static boolean esNumeroMayorQueCero(String dato){
		 int numero;
		 try{
			 numero = Integer.parseInt(dato);
		 }
		 catch(NumberFormatException e){
			 return false;
		 }
		 return (numero > 0);
	 }
	
	/**
	 * Formatea fechas
	 * @param fecha
	 * @return
	 */
	 public static String formateaFechas(Date fecha){
			
		SimpleDateFormat sf = new SimpleDateFormat(DATE_FORMAT);//"dd/MM/yyyy"
		return sf.format(fecha); 
	}
	 
	 
	 /**
	  * Obtiene el ano de la fecha actual
	  * @param year
	  * @return
	  */
	 public static String DateToYear(Date year){
		String formatoAux = "yyyy";
		
		SimpleDateFormat format = new SimpleDateFormat(formatoAux);
	
		String result=format.format(year);
		
		return result;
			
	}
		
	/**
	 * Obtiene fecha actual
	 * @return Fecha actual en formato string
	 */
	 public static String obtieneFechaActual(){
			Date fechaActual = new Date();
			SimpleDateFormat formato = new SimpleDateFormat(DATE_FORMAT);
			String cadenaFecha = formato.format(fechaActual);
			return cadenaFecha;		
	}

	 /**
	 * Compara las 2 fechas, y devuelve la que sea mayor
	 * @param fecha1
	 * @param fecha2
	 * @return
	 */public static Date comparaFechasMayor(Date fecha1, Date fecha2){
		 if(fecha1 != null && fecha1.compareTo(fecha2) > 0){
			return fecha1;
		}else
			return fecha2;	
	 }
		 
		
	 /**
	 * Compara las 2 fechas, y devuelve la que sea menor
	 * @param fecha1
	 * @param fecha2
	 * @return
	 */public static Date comparaFechasMenor(Date fecha1, Date fecha2){
			
		 if(fecha1 != null && fecha1.compareTo(fecha2) < 0){
			return fecha1;
		}else
			return fecha2;
	}
		 
		
		
	/**
	 * Indica si el numero es mayor o igual que 0 
	 * @param dato
	 * @return
	 */
	public static boolean esNumeroIgualMayorQueCero(String dato) {
		int numero;
		
		try{
		 numero = Integer.parseInt(dato);
		}
		catch (NumberFormatException e){
		 return false;
		}
	 
		return (numero >= 0);
	}
		
}
