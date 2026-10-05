# Roadmap

### 10.05.2026 0.0.1-SNAPSHOT

1. **Rework the `Application` class.** 
Review its lifecycle logic, set up proper restart handling, 
and make sure errors are caught and handled correctly.

2. **Audit exceptions.** 
Check that the right exceptions are thrown and that each one 
is caught at the appropriate level.

3. **Per-process logging.** 
Keep a separate log for each managed process so 
its output can be inspected later.

4. **`status` command.** 
Show the process's current state, its available commands, 
and a short tail of its recent log lines.
