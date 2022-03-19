package org.platform.modules.monitor.domain;

import java.util.HashMap;
import java.util.Map;

public class RedisInfo {

	private static Map<String, String> map = new HashMap<>();

	static {
		map.put("redis_version", "Redis Server Version");
		map.put("redis_git_sha1", "Git SHA1");
		map.put("redis_git_dirty", "Git dirty flag");
		map.put("os", "Redis The host operating system of the server");
		map.put("arch_bits", " Architecture (32 or 64 bits)");
		map.put("multiplexing_api", "Redis The event handling mechanism used");
		map.put("gcc_version", "The version of GCC used to compile Redis");
		map.put("process_id", "PID of the server process");
		map.put("run_id", "Redis Random identifiers for servers (for Sentinel and clustering)");
		map.put("tcp_port", "TCP/IP Listen on port");
		map.put("uptime_in_seconds", "The number of seconds that have elapsed since the Redis server started");
		map.put("uptime_in_days", "The number of days that have passed since the Redis server started");
		map.put("lru_clock", " Clock increment in minutes for LRU management");
		map.put("connected_clients", "Number of connected clients (excluding clients connected through slave servers)");
		map.put("client_longest_output_list", "The longest output list of currently connected clients");
		map.put("client_longest_input_buf", "Maximum input cache among currently connected clients");
		map.put("blocked_clients", "blocked_clients");
		map.put("used_memory", "used_memory");
		map.put("used_memory_human", "used_memory_human");
		map.put("used_memory_rss", "used_memory_rss");
		map.put("used_memory_peak", " used_memory_peak");
		map.put("used_memory_peak_human", "used_memory_peak_human");
		map.put("used_memory_lua", "used_memory_lua");
		map.put("mem_fragmentation_ratio", "mem_fragmentation_ratio");
		map.put("mem_allocator", "mem_allocator");

		map.put("redis_build_id", "redis_build_id");
		map.put("redis_mode", "redis_mode");
		map.put("atomicvar_api", "atomicvar_api");
		map.put("hz", "hz");
		map.put("executable", "executable");
		map.put("config_file", "config_file");
		map.put("client_biggest_input_buf", "client_biggest_input_buf");
		map.put("used_memory_rss_human", "used_memory_rss_human");
		map.put("used_memory_peak_perc", "used_memory_peak_perc");
		map.put("total_system_memory", "total_system_memory");
		map.put("total_system_memory_human", "total_system_memory_human");
		map.put("used_memory_lua_human", "used_memory_lua_human");
		map.put("maxmemory", "maxmemory");
		map.put("maxmemory_human", "maxmemory_human");
		map.put("maxmemory_policy", "maxmemory_policy");
		map.put("loading", "loading");
		map.put("rdb_changes_since_last_save", "rdb_changes_since_last_save");
		map.put("rdb_bgsave_in_progress", "rdb_bgsave_in_progress");
		map.put("rdb_last_save_time", "rdb_last_save_time");
		map.put("rdb_last_bgsave_status", "rdb_last_bgsave_status");
		map.put("rdb_last_bgsave_time_sec", "rdb_last_bgsave_time_sec");
		map.put("rdb_current_bgsave_time_sec", "rdb_current_bgsave_time_sec");
		map.put("aof_enabled", "aof_enabled");
		map.put("aof_rewrite_in_progress", "aof_rewrite_in_progress");
		map.put("aof_rewrite_scheduled", "aof_rewrite_scheduled");

		map.put("aof_last_rewrite_time_sec", "aof_last_rewrite_time_sec");
		map.put("aof_current_rewrite_time_sec", "aof_current_rewrite_time_sec");
		map.put("aof_last_bgrewrite_status", "aof_last_bgrewrite_status");
		map.put("aof_last_write_status", "aof_last_write_status");

		map.put("total_commands_processed", "total_commands_processed");
		map.put("total_connections_received", "total_connections_received");
		map.put("instantaneous_ops_per_sec", "instantaneous_ops_per_sec");
		map.put("total_net_input_bytes", "total_net_input_bytes");
		map.put("total_net_output_bytes", "total_net_output_bytes");

		map.put("instantaneous_input_kbps", "instantaneous_input_kbps");
		map.put("instantaneous_output_kbps", "instantaneous_output_kbps");
		map.put("rejected_connections", "rejected_connections");
		map.put("sync_full", "sync_full");

		map.put("sync_partial_ok", "sync_partial_ok");
		map.put("sync_partial_err", "sync_partial_err");
		map.put("expired_keys", "expired_keys");
		map.put("evicted_keys", "evicted_keys");
		map.put("keyspace_hits", "keyspace_hits");
		map.put("keyspace_misses", "keyspace_misses");
		map.put("pubsub_channels", "pubsub_channels");
		map.put("pubsub_patterns", "pubsub_patterns");
		map.put("latest_fork_usec", "latest_fork_usec");
		map.put("role", "role");
		map.put("connected_slaves", "connected_slaves");
		map.put("master_repl_offset", "master_repl_offset");
		map.put("repl_backlog_active", "repl_backlog_active");
		map.put("repl_backlog_size", "repl_backlog_size");
		map.put("repl_backlog_first_byte_offset", "repl_backlog_first_byte_offset");
		map.put("repl_backlog_histlen", "repl_backlog_histlen");
		map.put("used_cpu_sys", "used_cpu_sys");
		map.put("used_cpu_user", "used_cpu_user");
		map.put("used_cpu_sys_children", "used_cpu_sys_children");
		map.put("used_cpu_user_children", "used_cpu_user_children");
		map.put("cluster_enabled", "cluster_enabled");
		map.put("db0", "db0");

	}

	private String key;
	private String value;
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		this.description = map.get(this.key);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "RedisInfo{" + "key='" + key + '\'' + ", value='" + value + '\'' + ", desctiption='" + description + '\'' + '}';
	}
}
