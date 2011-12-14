
/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:jariera@emergya.com">José Alfonso Riera</a>
 * @author <a href="mailto:jsoler@emergya.com">Jaime Soler</a>
 * @author <a href="mailto:eserrano@emergya.com">Eduardo Serrano</a>
 *
 * This file is Component DataTransfer
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */


package com.emergya.aplicaciones.datatransfer;


/**
 * Class that represents an exception to the class DataTransfer
 * @author jariera
 * @version 1.0
 * 
 * @see DataTransfer
 */
public class DataTransferException extends Exception{

	
	/** Cause  of the exception */
	private Throwable rootCause = null;

	/**
	 * Constructor
	 */
	public DataTransferException(){
	}

	/**
	 * Constructor
	 * Add the message to the exception
	 * @param message
	 */
	public DataTransferException(String message) {
		super(message);
	}
		
	/**
	 * Add the cause to the exception
	 * @param rootCause
	 */
	public DataTransferException(Throwable rootCause) {
	    super(rootCause.getMessage());
		this.rootCause = rootCause;
	}

	/**
	 * Add the message and the cause of  the exception excepion DataTransfer
	 * @param message
	 * @param rootCause
	 */
	public DataTransferException(String message, Throwable rootCause) {
		super(message);
		this.rootCause = rootCause;
	}
	
	/**
	 * Gets the cause of the exception
	 * @return
	 */
	public Throwable getRootCause() {
		return rootCause;
	}
	
	/**
	 * Returns  the cause of the exception
	 */
	public String getMessage(){
		String m = super.getMessage();
		if(this.rootCause != null){
			m = m + " - Cause of Error: "+ this.rootCause.getMessage();
		}
		
		return m;
	}
	
}

