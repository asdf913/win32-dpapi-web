package javax.servlet.http;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

public class MainServlet extends HttpServlet {

	private static final long serialVersionUID = 6334507409143321232L;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		//
		try (final Writer writer = response != null ? response.getWriter() : null) {
			//
			if (writer != null) {
				//
				writer.write("Hello world");
				//
			} // if
				//
		} // try
			//
	}

}