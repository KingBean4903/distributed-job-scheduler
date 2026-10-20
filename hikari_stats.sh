
while true; do
  printf '\n%s ' "$(date +%H:%M:%S)"

  for metric in active idle pending; do
    value=$(curl -s \
      "http://localhost:9800/actuator/metrics/hikaricp.connections.${metric}" \
      | python3 -c '
import json,sys
d=json.load(sys.stdin)
print(next((m["value"] for m in d.get("measurements",[])
           if m["statistic"]=="VALUE"), "n/a"))
' 2>/dev/null)

    printf '%s=%s ' "$metric" "${value:-n/a}"
  done

  printf '\n  acquire: '

  curl -s \
    "http://localhost:9800/actuator/metrics/hikaricp.connections.acquire" \
    | python3 -c '
import json,sys
try:
    d=json.load(sys.stdin)
    measurements={m["statistic"]:m["value"] for m in d.get("measurements",[])}
    print("count={:.0f} total={:.6f}s max={:.6f}s".format(
        measurements.get("COUNT",0),
        measurements.get("TOTAL_TIME",0),
        measurements.get("MAX",0)))
except Exception:
    print("unavailable")
' 2>/dev/null

  sleep 1
done
