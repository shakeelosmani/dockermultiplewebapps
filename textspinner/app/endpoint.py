from flask import jsonify
from app import app
import re
import sys
import itertools
import more_itertools

def options(s):
	if len(s) > 0 and s[0] == '{':
		return [opt for opt in s[1:-1].split('|')]

	return [s]

@app.route("/spin/<message>")
def index(message):
  try:
    chunk = re.split('(\{[^\}]+\}|[^\{\}]+)', message)

    opt_lists = [options(frag) for frag in chunk]

    text = ''
    for spec in more_itertools.random_product(*opt_lists):
      text = text + (''.join(spec))

    return print(text)
  except Exception as e:
    return jsonify(str(e))
