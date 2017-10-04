{
  "targets": [
    {
      "include_dirs": [
        "<!(node -e \"require('nan')\")"
      ],
      "target_name": "cppaddon",
      "sources": [ "src/lib.cpp" ]
    }
  ]
}

