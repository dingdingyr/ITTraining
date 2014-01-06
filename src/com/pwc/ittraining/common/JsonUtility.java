package com.pwc.ittraining.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtility {
	private String[] getSplit(String json, char leftSplit, char rightSplit) {
		String[] result = null;
		if (json != null && json.length() >= 2) {
			char[] chars = json.toCharArray();
			chars[0] = chars[chars.length - 1] = ' ';
			for (int i = 0; i < chars.length - 2; i++) {
				char f = chars[i];
				if (i >= 1) {
					if (((f == ',' && chars[i - 1] == leftSplit && chars[i + 1] == rightSplit)
							|| (f == ',' && chars[i + 1] == rightSplit) || (f == ','
							&& chars[i - 1] == ']' && chars[i + 1] == rightSplit)) && (true)) {
						chars[i] = '#';
					}
				}
			}
			StringBuilder tmp = new StringBuilder(new String(chars));
			int start = 0;
			if ((start = tmp.indexOf(":[{")) != -1) {
				String as = tmp.substring(start, tmp.indexOf("}]") + 2);
				as = as.replaceAll("#", ",");
				tmp = tmp.replace(start, tmp.indexOf("}]") + 2, as);
			}
			if ((start = tmp.indexOf(":{")) != -1) {
				int end = indexOf(tmp, "}", start, 1);
				String as = tmp.substring(start, end - 1);
				as = as.replace("#", ",");
				tmp = tmp.replace(start, end - 1, as);
			}
			result = tmp.toString().trim().split("#");
			// return result.split("#");
		}
		return result;
	}

	private int indexOf(String org, String reg, int appearIndex) {
		if (org == null && reg == null && appearIndex <= 0) {
			return (-1);
		}
		int found = 0;
		int count = 0;
		while ((found = org.indexOf(reg)) != -1) {
			count++;
			if (appearIndex == count)
				break;
		}
		return found + count - 1;
	}

	private int indexOf(StringBuilder org, String reg, int appearIndex) {
		return indexOf(org.toString(), reg, appearIndex);
	}

	private int indexOf(String org, String reg, int start, int appear) {
		if (org == null && reg == null && appear <= 0) {
			return (-1);
		}
		String tmp = org.substring(start);
		int found = 0;
		int count = 0;
		while ((found = tmp.indexOf(reg)) != -1) {
			count++;
			if (appear == count)
				break;
		}
		return start + found + 2;
	}

	private int indexOf(StringBuilder org, String reg, int start, int appear) {
		return indexOf(org.toString(), reg, start, appear);
	}

	private void replace(String org, String str, int start, int end) {
		if (org == null && str == null) {
			return;
		}
		int length = org.length();
		if (length < start || start < 0) {
			return;
		}
		StringBuilder sb = new StringBuilder(org);
		sb = sb.replace(start, end > length ? length : end, str);
	}

	private String[] getMSplit(String json) {
		if (containsJsonArray(json)) {
			json = json.replaceFirst(":", "#");
			return json.trim().split("#");
		} else if (containsJsonObj(json)) {
			json = json.replaceFirst(":", "#");
			return json.trim().split("#");
		}
		return json.split(":");
	}

	private boolean containsJsonArray(String json) {
		return json != null && json.contains("[{") && json.contains("}]");
	}

	private boolean containsJsonObj(String json) {
		return json != null && json.contains(":{") && json.contains("}");
	}

	public Map parseSingleObject(String json) {
		Map m = new HashMap();
		String[] s = this.getSplit(json, '"', '"');
		if (s != null && s.length > 0) {
			for (int i = 0; i < s.length; i++) {
				String[] keys = this.getMSplit(s[i]);

				String key = replace(keys[0]);
				String value = keys[1].trim();

				if (isJsonArray(value)) {
					List list = this.parseList(value);
					m.put(key, list);
				} else if (isJsonObject(value)) {
					Map d = this.parseSingleObject(value);
					m.put(key, d);
				} else {
					m.put(key, replace(value));
				}
			}
		}

		return m;
	}

	private boolean isJsonArray(String array) {
		return array != null && array.startsWith("[") && array.endsWith("]");
	}

	private boolean isJsonObject(String map) {
		return map != null && map.startsWith("{") && map.endsWith("}");
	}

	public List<Map> parseList(String json) {
		if (json.startsWith("[") && json.endsWith("]")) {
			String[] items = this.getSplit(json, '}', '{');
			List<Map> rs = null;
			if (items != null && items.length > 0) {
				rs = new ArrayList<Map>();
				for (int i = 0; i < items.length; i++) {
					rs.add(this.parseSingleObject(items[i]));
				}
			}
			return rs;
		}
		return null;
	}

	private String replace(String src) {
		return src.replaceAll("\"", "").trim();
	}
}