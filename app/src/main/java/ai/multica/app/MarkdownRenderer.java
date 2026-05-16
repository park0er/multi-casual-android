package ai.multica.app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.ViewGroup;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

final class MarkdownRenderer {
    private MarkdownRenderer() {}

    static final class Palette {
        final int blockBackground;
        final int tableHeaderBackground;
        final int tableCellBackground;

        private Palette(int blockBackground, int tableHeaderBackground, int tableCellBackground) {
            this.blockBackground = blockBackground;
            this.tableHeaderBackground = tableHeaderBackground;
            this.tableCellBackground = tableCellBackground;
        }
    }

    static Palette paletteFor(int textColor, int mutedColor, int borderColor) {
        boolean darkSurface = luminance(textColor) > luminance(borderColor);
        if (darkSurface) {
            return new Palette(0xFF1C1C1E, 0xFF1C2638, 0xFF111113);
        }
        return new Palette(0xFFF3F4F6, 0xFFEFF6FF, 0xFFFFFFFF);
    }

    static void render(Context context, LinearLayout parent, String markdown, int textColor, int mutedColor, int borderColor) {
        parent.removeAllViews();
        Palette palette = paletteFor(textColor, mutedColor, borderColor);
        if (markdown == null || markdown.trim().isEmpty()) {
            TextView empty = text(context, "", 15, textColor);
            parent.addView(empty);
            return;
        }

        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        List<String> paragraph = new ArrayList<>();
        boolean inCode = false;
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().startsWith("```")) {
                flushParagraph(context, parent, paragraph, textColor);
                if (inCode) {
                    addCodeBlock(context, parent, code.toString(), mutedColor, palette);
                    code.setLength(0);
                    inCode = false;
                } else {
                    inCode = true;
                }
                continue;
            }
            if (inCode) {
                code.append(line).append('\n');
                continue;
            }

            if (isTableStart(lines, i)) {
                flushParagraph(context, parent, paragraph, textColor);
                List<String> table = new ArrayList<>();
                table.add(lines[i]);
                i += 2;
                while (i < lines.length && lines[i].trim().startsWith("|") && lines[i].contains("|")) {
                    table.add(lines[i]);
                    i++;
                }
                i--;
                addTable(context, parent, table, textColor, mutedColor, borderColor, palette);
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                flushParagraph(context, parent, paragraph, textColor);
            } else if (trimmed.matches("^(-{3,}|\\*{3,}|_{3,})$")) {
                flushParagraph(context, parent, paragraph, textColor);
                addDivider(context, parent, borderColor);
            } else if (trimmed.startsWith("#")) {
                flushParagraph(context, parent, paragraph, textColor);
                addHeading(context, parent, trimmed, textColor);
            } else if (trimmed.startsWith(">")) {
                flushParagraph(context, parent, paragraph, textColor);
                TextView quote = text(context, trimmed.replaceFirst("^>\\s?", ""), 15, mutedColor);
                quote.setPadding(dp(context, 10), dp(context, 6), dp(context, 8), dp(context, 6));
                quote.setBackgroundColor(palette.blockBackground);
                parent.addView(quote);
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                flushParagraph(context, parent, paragraph, textColor);
                TextView bullet = text(context, "• " + trimmed.substring(2), 15, textColor);
                bullet.setPadding(dp(context, 8), dp(context, 2), 0, dp(context, 2));
                parent.addView(bullet);
            } else {
                paragraph.add(line);
            }
        }
        flushParagraph(context, parent, paragraph, textColor);
        if (code.length() > 0) addCodeBlock(context, parent, code.toString(), mutedColor, palette);
    }

    private static boolean isTableStart(String[] lines, int index) {
        if (index + 1 >= lines.length) return false;
        String header = lines[index].trim();
        String divider = lines[index + 1].trim();
        return header.startsWith("|") && header.endsWith("|")
                && divider.matches("^\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?$");
    }

    private static void flushParagraph(Context context, LinearLayout parent, List<String> paragraph, int textColor) {
        if (paragraph.isEmpty()) return;
        TextView p = text(context, String.join("\n", paragraph), 15, textColor);
        p.setText(applyInline(p.getText().toString()));
        p.setMovementMethod(LinkMovementMethod.getInstance());
        p.setPadding(0, dp(context, 2), 0, dp(context, 6));
        parent.addView(p);
        paragraph.clear();
    }

    private static void addHeading(Context context, LinearLayout parent, String raw, int textColor) {
        int level = 0;
        while (level < raw.length() && raw.charAt(level) == '#') level++;
        String title = raw.substring(level).trim();
        TextView h = text(context, title, level <= 1 ? 22 : level == 2 ? 19 : 17, textColor);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setPadding(0, dp(context, 8), 0, dp(context, 4));
        parent.addView(h);
    }

    private static void addCodeBlock(Context context, LinearLayout parent, String code, int mutedColor, Palette palette) {
        TextView tv = text(context, code.trim(), 13, mutedColor);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8));
        tv.setBackgroundColor(palette.blockBackground);
        parent.addView(tv);
    }

    private static void addDivider(Context context, LinearLayout parent, int borderColor) {
        View line = new View(context);
        line.setBackgroundColor(borderColor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(context, 1)));
        params.setMargins(0, dp(context, 8), 0, dp(context, 8));
        parent.addView(line, params);
    }

    private static void addTable(Context context, LinearLayout parent, List<String> lines, int textColor, int mutedColor, int borderColor, Palette palette) {
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        TableLayout table = new TableLayout(context);
        table.setShrinkAllColumns(false);
        table.setStretchAllColumns(false);

        for (int r = 0; r < lines.size(); r++) {
            String[] cells = splitTable(lines.get(r));
            TableRow row = new TableRow(context);
            for (String cell : cells) {
                TextView tv = text(context, "", 14, r == 0 ? textColor : mutedColor);
                tv.setText(applyInline(cell.trim()));
                tv.setTypeface(r == 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                tv.setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8));
                tv.setBackgroundColor(r == 0 ? palette.tableHeaderBackground : palette.tableCellBackground);
                row.addView(tv);
            }
            table.addView(row);
        }
        table.setBackgroundColor(borderColor);
        table.setPadding(1, 1, 1, 1);
        scroll.addView(table);
        scroll.setPadding(0, dp(context, 6), 0, dp(context, 8));
        parent.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    static String[] splitTable(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("|")) trimmed = trimmed.substring(1);
        if (endsWithUnescapedPipe(trimmed)) trimmed = trimmed.substring(0, trimmed.length() - 1);

        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inCode = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (ch == '`') {
                inCode = !inCode;
                cell.append(ch);
            } else if (ch == '\\' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '|') {
                cell.append('|');
                i++;
            } else if (ch == '|' && !inCode) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(ch);
            }
        }
        cells.add(cell.toString());
        return cells.toArray(new String[0]);
    }

    private static boolean endsWithUnescapedPipe(String value) {
        if (!value.endsWith("|")) return false;
        int slashCount = 0;
        for (int i = value.length() - 2; i >= 0 && value.charAt(i) == '\\'; i--) {
            slashCount++;
        }
        return slashCount % 2 == 0;
    }

    private static double luminance(int color) {
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    private static TextView text(Context context, String value, int sp, int color) {
        TextView tv = new TextView(context);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setLineSpacing(0, 1.08f);
        return tv;
    }

    private static SpannableStringBuilder applyInline(String value) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        int i = 0;
        while (i < value.length()) {
            if (value.startsWith("**", i)) {
                int end = value.indexOf("**", i + 2);
                if (end > i + 2) {
                    int start = out.length();
                    out.append(value, i + 2, end);
                    out.setSpan(new StyleSpan(Typeface.BOLD), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 2;
                    continue;
                }
            }
            if (value.charAt(i) == '`') {
                int end = value.indexOf('`', i + 1);
                if (end > i + 1) {
                    int start = out.length();
                    out.append(value, i + 1, end);
                    out.setSpan(new TypefaceSpan("monospace"), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = end + 1;
                    continue;
                }
            }
            out.append(value.charAt(i));
            i++;
        }
        return out;
    }

    static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
