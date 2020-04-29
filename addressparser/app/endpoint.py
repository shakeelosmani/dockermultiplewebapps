from flask import jsonify
from flask import request
from app import app
import sys
import usaddress

@app.route("/parseaddress", methods=['POST'])
def parseaddress():
  try:
    inp = request.json
    address = inp["address"]
    processedaddress = usaddress.tag(address)

    return jsonify(processedaddress)
  except Exception as e:
    return jsonify(str(e))

@app.route("/parseaddresswithmapping", methods=['POST'])
def parseaddresswithmapping():
  try:
    inp = request.json
    address = inp["address"]
    processedaddress = usaddress.tag(address, tag_mapping = {
      'Recipient': 'recipient',
      'AddressNumber': 'address1',
      'AddressNumberPrefix': 'address1',
      'AddressNumberSuffix': 'address1',
      'StreetName': 'address1',
      'StreetNamePreDirectional': 'address1',
      'StreetNamePreModifier': 'address1',
      'StreetNamePreType': 'address1',
      'StreetNamePostDirectional': 'address1',
      'StreetNamePostModifier': 'address1',
      'StreetNamePostType': 'address1',
      'CornerOf': 'address1',
      'IntersectionSeparator': 'address1',
      'LandmarkName': 'address1',
      'USPSBoxGroupID': 'address1',
      'USPSBoxGroupType': 'address1',
      'USPSBoxID': 'address1',
      'USPSBoxType': 'address1',
      'BuildingName': 'address2',
      'OccupancyType': 'address2',
      'OccupancyIdentifier': 'address2',
      'SubaddressIdentifier': 'address2',
      'SubaddressType': 'address2',
      'PlaceName': 'city',
      'StateName': 'state',
      'ZipCode': 'zipcode'
    })

    return jsonify(processedaddress)
  except Exception as e:
    return jsonify(str(e))
