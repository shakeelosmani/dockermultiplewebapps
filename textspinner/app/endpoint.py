from flask import jsonify
from flask import request
from app import app
import re
import sys
import itertools
import more_itertools

def options(s):
	if len(s) > 0 and s[0] == '{':
		return [opt for opt in s[1:-1].split('|')]

	return [s]

@app.route("/spin", methods=['POST'])
def index():
  try:
    inp = request.json
    message = inp["message"]
    chunk = re.split('(\{[^\}]+\}|[^\{\}]+)', message)

    opt_lists = [options(frag) for frag in chunk]

    text = ''
    for spec in more_itertools.random_product(*opt_lists):
      text = text + (''.join(spec))

    return jsonify(text)
  except Exception as e:
    return jsonify(str(e))
