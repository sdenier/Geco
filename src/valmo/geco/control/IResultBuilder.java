/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.control;

import java.io.IOException;

import valmo.geco.control.ResultBuilder.ResultConfig;

/**
 * @author Simon Denier
 * @since Nov 7, 2010
 *
 */
public interface IResultBuilder {

	public String generateHtmlResults(ResultConfig config, int refreshDelay);

	public void exportFile(String filename, String exportFormat, ResultConfig config, int refreshDelay)
					throws IOException;

}
