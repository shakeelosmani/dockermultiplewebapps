from flask import jsonify
from flask import request
from app import app
import sys
import usaddress

@app.route("/parseaddress", methods=['POST'])
def index():
  try:
    inp = request.json
    address = inp["address"]
    processedaddress = usaddress.tag(address)

    return jsonify(processedaddress)
  except Exception as e:
    return jsonify(str(e))
