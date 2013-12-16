/**
 * 
 */
package org.varunverma.desijokes;

/**
 * @author varun
 *
 */
public class Constants {

	private static boolean premiumVersion;
	private static String productTitle, productDescription, productPrice;
	
	static String getPublicKey() {
		return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArelQc1O1dRTgbd4N451Di09" +
				"jXN0qGjg337tAW5wShXdVsC0bVRT7soSnVOeTUsJihJtOof6MOXM2tso+XVFp/9y/S" +
				"BZeZzC0HZSefq+pHJ3IhhRB+6aytULAYR9gc2KoZPAl8zlUrZCjSSaa9fpTVQrrBX5" +
				"5EN2ZmsmHmfOYqoDpApFODs5oHU7JRUtufPKhGTQSYSDl/Z4eMjBgod06rJwIoi9B+" +
				"+a0R7oVKrmiGuSAHF/rdnOK9n6V2vfKwfwPST09AlQh+EvxMPORsyhViDIfnpUJCKx" +
				"gmvzCZXewMYKYXsNjM8NreZFXFtkeXkPee4w+CTJfbinQTRsyKH4xfwIDAQAB";
	}

	static String getProductKey() {
		return "premium_content";
	}

	static void setPremiumVersion(boolean premiumVersion) {
		Constants.premiumVersion = premiumVersion;
	}
	
	static boolean isPremiumVersion(){
		return premiumVersion;
	}

	/**
	 * @return the productTitle
	 */
	static String getProductTitle() {
		return productTitle;
	}

	/**
	 * @param productTitle the productTitle to set
	 */
	static void setProductTitle(String productTitle) {
		Constants.productTitle = productTitle;
	}

	/**
	 * @return the productDescription
	 */
	static String getProductDescription() {
		return productDescription;
	}

	/**
	 * @param productDescription the productDescription to set
	 */
	static void setProductDescription(String productDescription) {
		Constants.productDescription = productDescription;
	}

	/**
	 * @return the productPrice
	 */
	static String getProductPrice() {
		return productPrice;
	}

	/**
	 * @param productPrice the productPrice to set
	 */
	static void setProductPrice(String productPrice) {
		Constants.productPrice = productPrice;
	}

}