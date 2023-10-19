class OtaFile {
  final String name;
  OtaFile({required this.name});
  Map<String, dynamic> toJson() {
    return {
      'name': name,
    };
  }
}