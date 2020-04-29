from flask import jsonify
from app import app
from nameparser import HumanName

@app.route("/parse/<name>")
def parse(name):
  try:
    parsed_name = HumanName(name)
    parsed_name.capitalize(force=True)

    return jsonify(parsed_name.as_dict())
  except Exception as e:
    return jsonify(str(e))

@app.route("/capitalize/<name>")
def capitalize(name):
  try:
    parsed_name = HumanName(name)
    parsed_name.capitalize(force=True)

    return jsonify(str(parsed_name))
  except Exception as e:
    return jsonify(str(e))
