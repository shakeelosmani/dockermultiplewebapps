from flask import jsonify
from app import app
from nameparser import HumanName


@app.route("/parse/<name>")
def index(name):
  try:
    return_name = HumanName(name)
    return jsonify(return_name.as_dict())
  except Exception as e:
    return jsonify(str(e))
