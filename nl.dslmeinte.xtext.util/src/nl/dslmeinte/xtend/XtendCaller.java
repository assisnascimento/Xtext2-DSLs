package nl.dslmeinte.xtend;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.expression.Variable;
import org.eclipse.xtend.typesystem.MetaModel;

/**
 * Util class to programmatically call Xtend transformation. This is useful for
 * quickly and easily implementing m2m transformations in Xtend which might
 * ultimately not be performant enough but for which an interpreted performance
 * is good enough for now.
 * 
 * @deprecated Use Xtend2 instead of Xpand/Xtend.
 * 
 * @author Meinte Boersma (adaptation)
 * @author Marc Schlienger (original; (c) 2009, see http://www.innoq.com/blog/mrs)
 */
@Deprecated
public final class XtendCaller {

	private XtendCaller() {
		// (prevent instantiation)
	}

	public static Object evaluateExtensionWithGlobalVars(
			String extensionFile,
			String extension,
			MetaModel[] metaModels,
			Map<String, Variable> globalVarsToInject,
			Object...parameters
		)
	{
		XtendFacade facade = XtendFacade.create(extensionFile);

		// acquire global variables of the current execution context:
		@SuppressWarnings( "unchecked" )
		Map<String, Variable> actualGlobalVars =
			(Map<String, Variable>) getPrivateField( getPrivateField(facade, "ctx"), "globalVars" ); //$NON-NLS-1$ //$NON-NLS-2$
		// inject programmatically passed ones into it:
		actualGlobalVars.putAll(globalVarsToInject);

		for( MetaModel metaModel : metaModels ) {
			facade.registerMetaModel(metaModel);
		}

		return facade.call(extension, parameters);
	}

	private static Object getPrivateField(Object obj, String fieldName) {
		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			throw new RuntimeException(
					"couldn't obtain (value of) private field " //$NON-NLS-1$
					+ obj.getClass().getName() + "#" + fieldName, //$NON-NLS-1$
					e
				);
		}
	}

	public static Object evaluateExtension(
			String extensionFile,
			String extension,
			MetaModel[] metaModels,
			Object...parameters
		)
	{
		return evaluateExtensionWithGlobalVars(extensionFile, extension, metaModels, Collections.<String, Variable>emptyMap(), parameters);
	}

}

