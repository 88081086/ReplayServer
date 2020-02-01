package pengguang.replayserver;

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.text.Collator;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Servlet implementation class LoginController
 */
public class Replay extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String entry = request.getParameter("entry");
		if (entry == null) {
			entry = "";
		} else {
		    entry = URLDecoder.decode(entry, "UTF-8");
        }

		out.println("<HTML>");
		out.println("<HEAD><meta http-equiv=Content-Type content='text/html;charset=utf-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'><TITLE>棋谱</TITLE>");
		out.println("<link rel='stylesheet' type='text/css' href='css/style.css'>");
		out.println("<script src='js/misc.js'></script>");
		out.println("<script src='js/chess.js'></script>");
		out.println("</HEAD>");
		out.println("<BODY>");

        String parent = "";
        int pos = entry.lastIndexOf("/");
        if (pos != -1) {
            parent = entry.substring(0, pos);
        }

		File path = new File(Util.ROOT_PATH+"/"+entry);
		if (!path.isDirectory()) {
            out.println("<table width='100%'><tr><td valign='top'><form action='index.html' style='display: inline;'> <input type='hidden' name='entry' value='"+parent+"'/><input type='submit' value='返回' /></form></td><td align='right' valign='top'><form action='index.html' method='post'><input type='hidden' name='entry' value='"+entry+"'/><input type='hidden' name='action' value='delete'/><input type='submit' value='删除'/></form></td></tr></table>");
		    Loader loader = Loader.loadReplay(new File(Util.ROOT_PATH+"/"+entry));
		    if (loader != null) {
                ArrayList list =  loader.mList;
                int i;
                String config = "";
                String s = "";
                if (loader.mInitData != null) {
                    for (i=0; i<loader.mInitData.length; i++) s += String.format("%04x", loader.mInitData[i]);
                    config += String.format("init: \"%s\", ", s);
                }

                s = "";
                for (i=0; i<list.size(); i++) {
                    ChessRoute r = (ChessRoute) ((HashMap<String, Object>)list.get(i)).get("route");
                    s += r.y1();
                    s += r.x1();
                    s += r.y2();
                    s += r.x2();
                }
                config += String.format("movelist: \"%s\", ", s);

                if (loader.mRed.length() != 0) config += String.format("red: \"%s\", ", loader.mRed);
                if (loader.mBlack.length() != 0) config += String.format("black: \"%s\", ", loader.mBlack);
                if (loader.mDate.length() != 0) config += String.format("date: \"%s\", ", loader.mDate);
                if (loader.mSite.length() != 0) config += String.format("site: \"%s\", ", loader.mSite);
                if (loader.mEvent.length() != 0) config += String.format("event: \"%s\", ", loader.mEvent);
		        out.println("<div id='chess'></div><script>document.querySelector('#chess').appendChild(startGame({"+config+"}));</script>");
            }
        } else {
            out.println("<table width='100%'><tr><td valign='top'><form action='index.html' style='display: inline;'> <input type='hidden' name='entry' value='"+parent+"'/><input type='submit' value='返回' /></form></td><td align='right' valign='top'><button class='button' onclick='toggleUploadDiv()'>添加</button></td></tr></table>");
            out.println("<div id='uploadDiv' style='display: none'><form action='index.html' style='display: inline;' method='post' enctype='multipart/form-data'><table width='100%'><tr><td></td><td align='right'><input type='file' name='file' accept='.pgn, .xqr, .xqf' multiple='true'/><input type='hidden' name='entry' value='"+entry+"'/><input type='submit' value='上传'/></td></tr></table></form></div>");
            File[] files = path.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isHidden())
                        return false;
                    if (file.isDirectory())
                        return true;

                    String name = file.getName();
                    if (name.substring(name.lastIndexOf('.')+1).toLowerCase().matches("xqr|xqf|pgn")) return true;
                    return false;
                }
            });
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        Collator cmp = Collator.getInstance();
                        if (f1.isDirectory()) {
                            if (f2.isDirectory()) {
                                return cmp.compare(f1.getName(), f2.getName());
                            } else {
                                return -1;
                            }
                        } else {
                            if (!f2.isDirectory()) {
                                return cmp.compare(f1.getName(), f2.getName());
                            } else {
                                return 1;
                            }
                        }
                    }
                });
            }

            out.println("<table width='100%'>");
            for (File file: files) {
                String name = file.getName();
                if (file.isDirectory()) {
                    out.println(String.format("<tr><td><a href='index.html?entry=%s'><b>%s</b></a></td></tr>", URLEncoder.encode(String.format("%s/%s", entry, name), "UTF-8"), name));
                } else {
                    out.println(String.format("<tr><td><a href='index.html?entry=%s'>%s</a></td></tr>", URLEncoder.encode(String.format("%s/%s", entry, name), "UTF-8"), name));
                }
            }
            out.println("</table>");
        }

		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");

		String entry = request.getParameter("entry");
		if (entry == null) {
			entry = "";
		} else {
		    entry = URLDecoder.decode(entry, "UTF-8");
        }

		String action = request.getParameter("action");
		if (action != null && action.equals("delete")) {
		    new File(String.format("%s/%s", Util.ROOT_PATH, entry)).delete();

            String parent = "";
            int pos = entry.lastIndexOf("/");
            if (pos != -1) {
                parent = entry.substring(0, pos);
            }
		    response.sendRedirect("index.html?entry="+URLEncoder.encode(parent, "UTF-8"));
        } else {
            List<Part> fileParts = request.getParts().stream().filter(part -> "file".equals(part.getName())).collect(Collectors.toList()); // Retrieves <input type="file" name="file" multiple="true">
            for (Part filePart : fileParts) {
                try {
                    String filename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
                    InputStream fileContent = filePart.getInputStream();
                    String path = String.format("%s/%s/%s", Util.ROOT_PATH, entry, filename);
                    Log.d("upload "+path);
                    File file = new File(path);
                    if (file.exists()) {
                        continue;
                    }

                    byte[] buf = new byte[512];
                    int len = 0;
                    FileOutputStream os = new FileOutputStream(file);
                    while ((len = fileContent.read(buf)) > 0) {
                        os.write(buf, 0, len);
                    }
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

		    response.sendRedirect("index.html?entry="+URLEncoder.encode(entry, "UTF-8"));
        }
	}
}

