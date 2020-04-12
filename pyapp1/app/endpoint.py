from flask import jsonify
from app import app


@app.route("/hello/<name>")
def index(name):
  try:
    return jsonify('Hello ' + str(name))
  except Exception as e:
    return jsonify(str(e))
