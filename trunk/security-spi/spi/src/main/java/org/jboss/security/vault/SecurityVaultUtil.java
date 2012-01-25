/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.security.vault;

import java.util.StringTokenizer;

import org.jboss.security.vault.SecurityVault;
import org.jboss.security.vault.SecurityVaultException;
import org.jboss.security.vault.SecurityVaultFactory;

/**
 * Common utility methods associated with the {@link SecurityVault}
 * 
 * @author anil saldhana
 */
public class SecurityVaultUtil 
{
	public static final String VAULT_PREFIX = "VAULT";

	/**
	 * Check whether the string has the format of the vault
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isVaultFormat(char[] chars) 
	{
		if(chars == null) 
		{
			return false;
		}
		String str = new String(chars);
		return str.startsWith(VAULT_PREFIX);
	}
	
	/**
	 * Check whether the string has the format of the vault
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isVaultFormat(String str) 
	{
		return str != null && str.startsWith(VAULT_PREFIX);
	}

	/**
	 * <p>
	 * Given the vault formatted string, retrieve the attribute value from the
	 * vault
	 * </p>
	 * <p>
	 * Note: the vault formatted string will be of the form
	 * VAULT::vault_block::attribute_name::sharedKey
	 * </p>
	 * 
	 * <p>
	 * Vault Block acts as the unique id of a block such as "messaging",
	 * "security" etc Attribute Name is the name of the attribute whose value we
	 * are preserving Shared Key is the key generated by the off line vault
	 * during storage of the attribute value
	 * </p>
	 * 
	 * @param vaultString
	 * @return
	 * @throws SecurityVaultException
	 */
	public static char[] getValue(String vaultString)
			throws SecurityVaultException 
    {
		if (!isVaultFormat(vaultString))
			throw new IllegalArgumentException(
					"vaultString is of the wrong format:" + vaultString);
		String[] tokens = tokens(vaultString);

		SecurityVault vault = SecurityVaultFactory.get();
		if (!vault.isInitialized())
			throw new SecurityVaultException("Vault is not initialized");
		return vault.retrieve(tokens[1], tokens[2], tokens[3].getBytes());
	}

	/**
	 * @see #getValue(String)
	 * @param vaultString
	 * @return
	 * @throws SecurityVaultException
	 */
	public static String getValueAsString(String vaultString)
			throws SecurityVaultException 
	{
		char[] val = getValue(vaultString);
		if (val != null)
			return new String(val);
		return null;
	}
	
	/**
	 * Get the value from the vault
	 * @param chars vaultified set of characters
	 * @return
	 * @throws SecurityVaultException
	 */
	public static char[] getValue(char[] chars)
			throws SecurityVaultException 
	{
		if(chars == null)
			return null;
		String vaultString = new String(chars);
		return getValue(vaultString);
	}

	private static String[] tokens(String vaultString) 
	{
		StringTokenizer tokenizer = new StringTokenizer(vaultString, "::");
		int length = tokenizer.countTokens();
		String[] tokens = new String[length];

		int index = 0;
		while (tokenizer != null && tokenizer.hasMoreTokens()) 
		{
			tokens[index++] = tokenizer.nextToken();
		}
		return tokens;
	}
}