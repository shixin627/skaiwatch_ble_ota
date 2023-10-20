class OtaModel {
  final String? address;
  final String fileName;
  OtaModel({this.address, required this.fileName});
  Map<String, dynamic> toJson() {
    return {
      'address': address,
      'fileName': fileName,
    };
  }
}