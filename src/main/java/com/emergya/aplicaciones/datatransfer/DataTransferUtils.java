package com.emergya.aplicaciones.datatransfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Clase para
 * @author jariera
 * @version 1.0
 */
public class DataTransferUtils {
	/**
	 * Mapa de propiedades complejas por Clase (para su uso con DataTransfer).
	 * Map(Class claseAConvertir, Map(String, Class) atributosComplejos)
	 */
	public static final Map MAPA_ATRIBUTOS_COMPLEJOS_DTO = new HashMap();
	static{}
	
	/**
	 * Mapa de clases de pojo por clase de dto
	 */
	public static final Map MAPA_CLASES_POJO_POR_CLASES_DTO = new HashMap<Class<?>,Class<?>>();
	static{
		/* La inicializacion del mapa debiera de ser dinamicamente
		 * 
		 * Rellenar con los dto y pojos usados en la aplicacion
		MAPA_CLASES_POJO_POR_CLASES_DTO.put(com.emergya.aplicaciones.venezuelacert.services.acceso.dto.UsuarioDto.class, 
				com.emergya.aplicaciones.venezuelacert.model.dao.acceso.entity.UsuarioEntity.class);
		*/
	}

	/**
	 * Obtiene la clase del pojo asociada al a clase del dto pasada
	 * 
	 * @param claseDto
	 * @return
	 */
	public static Class getClasePojoPorClaseDto(Class claseDto){
		if(MAPA_CLASES_POJO_POR_CLASES_DTO.containsKey(claseDto)){
			return (Class) MAPA_CLASES_POJO_POR_CLASES_DTO.get(claseDto);
		}else{
			return null;
		}
	}

	/**
	 * Obtiene la clase del dto asociada al a clase del pojo pasada
	 * 
	 * @param clasePojo
	 * @return
	 */
	public static Class getClaseDtoPorClasePojo(Class clasePojo){
		Iterator it = MAPA_CLASES_POJO_POR_CLASES_DTO.values().iterator();
		while (it.hasNext()){
			Class claseActual = (Class) it.next();
			if(claseActual.equals(clasePojo)){
				//Encontrada
				return claseActual;
			}
		}
		//No se encuentra
		return null;
	}
}
