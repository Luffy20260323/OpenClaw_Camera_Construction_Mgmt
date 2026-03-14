--
-- PostgreSQL database dump
--

\restrict uCZ11gMUbhG4TbanwBcT7ogRCPvfDQh9aLFPEy2LqC1s5gW9mlTyRnjd580KnRq

-- Dumped from database version 16.13 (Ubuntu 16.13-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.13 (Ubuntu 16.13-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.work_areas DROP CONSTRAINT IF EXISTS work_areas_company_id_fkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_company_id_fkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_approved_by_fkey;
ALTER TABLE IF EXISTS ONLY public.user_work_areas DROP CONSTRAINT IF EXISTS user_work_areas_work_area_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_work_areas DROP CONSTRAINT IF EXISTS user_work_areas_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_role_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_role_requests DROP CONSTRAINT IF EXISTS user_role_requests_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_role_requests DROP CONSTRAINT IF EXISTS user_role_requests_role_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_online_logs DROP CONSTRAINT IF EXISTS user_online_logs_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.team_members DROP CONSTRAINT IF EXISTS team_members_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.team_members DROP CONSTRAINT IF EXISTS team_members_team_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignments DROP CONSTRAINT IF EXISTS task_assignments_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignments DROP CONSTRAINT IF EXISTS task_assignments_team_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignments DROP CONSTRAINT IF EXISTS task_assignments_section_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignments DROP CONSTRAINT IF EXISTS task_assignments_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignment_users DROP CONSTRAINT IF EXISTS task_assignment_users_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.task_assignment_users DROP CONSTRAINT IF EXISTS task_assignment_users_task_id_fkey;
ALTER TABLE IF EXISTS ONLY public.table_structure_registry DROP CONSTRAINT IF EXISTS table_structure_registry_component_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_audits DROP CONSTRAINT IF EXISTS subtask_scrap_audits_auditor_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_audits DROP CONSTRAINT IF EXISTS subtask_scrap_audits_application_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_applications DROP CONSTRAINT IF EXISTS subtask_scrap_applications_subtask_instance_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_applications DROP CONSTRAINT IF EXISTS subtask_scrap_applications_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_applications DROP CONSTRAINT IF EXISTS subtask_scrap_applications_applicant_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_instances DROP CONSTRAINT IF EXISTS subtask_instances_subtask_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_instances DROP CONSTRAINT IF EXISTS subtask_instances_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_definitions DROP CONSTRAINT IF EXISTS subtask_definitions_predecessor_subtask_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_audit_records DROP CONSTRAINT IF EXISTS subtask_audit_records_subtask_instance_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_audit_records DROP CONSTRAINT IF EXISTS subtask_audit_records_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.subtask_audit_records DROP CONSTRAINT IF EXISTS subtask_audit_records_auditor_id_fkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_company_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.role_permissions DROP CONSTRAINT IF EXISTS role_permissions_role_id_fkey;
ALTER TABLE IF EXISTS ONLY public.role_permissions DROP CONSTRAINT IF EXISTS role_permissions_permission_id_fkey;
ALTER TABLE IF EXISTS ONLY public.risk_rectification_items DROP CONSTRAINT IF EXISTS risk_rectification_items_subtask_instance_id_fkey;
ALTER TABLE IF EXISTS ONLY public.risk_rectification_items DROP CONSTRAINT IF EXISTS risk_rectification_items_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.risk_rectification_items DROP CONSTRAINT IF EXISTS risk_rectification_items_audit_id_fkey;
ALTER TABLE IF EXISTS ONLY public.projects DROP CONSTRAINT IF EXISTS projects_project_manager_id_fkey;
ALTER TABLE IF EXISTS ONLY public.projects DROP CONSTRAINT IF EXISTS projects_company_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_sections DROP CONSTRAINT IF EXISTS project_sections_supervisor_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_sections DROP CONSTRAINT IF EXISTS project_sections_section_leader_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_sections DROP CONSTRAINT IF EXISTS project_sections_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_sections DROP CONSTRAINT IF EXISTS project_sections_contractor_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_subtask_instance_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_subtask_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_section_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_confirmed_by_fkey;
ALTER TABLE IF EXISTS ONLY public.project_authorizations DROP CONSTRAINT IF EXISTS project_authorizations_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_authorizations DROP CONSTRAINT IF EXISTS project_authorizations_granted_by_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.project_authorizations DROP CONSTRAINT IF EXISTS project_authorizations_authorized_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_audits DROP CONSTRAINT IF EXISTS progress_payment_audits_auditor_id_fkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_audits DROP CONSTRAINT IF EXISTS progress_payment_audits_application_id_fkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_applications DROP CONSTRAINT IF EXISTS progress_payment_applications_section_id_fkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_applications DROP CONSTRAINT IF EXISTS progress_payment_applications_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.points DROP CONSTRAINT IF EXISTS points_work_area_id_fkey;
ALTER TABLE IF EXISTS ONLY public.points DROP CONSTRAINT IF EXISTS points_section_id_fkey;
ALTER TABLE IF EXISTS ONLY public.point_status DROP CONSTRAINT IF EXISTS point_status_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.page_access_logs DROP CONSTRAINT IF EXISTS page_access_logs_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.operation_logs DROP CONSTRAINT IF EXISTS operation_logs_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.inspection_records DROP CONSTRAINT IF EXISTS inspection_records_reviewer_id_fkey;
ALTER TABLE IF EXISTS ONLY public.inspection_records DROP CONSTRAINT IF EXISTS inspection_records_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.inspection_records DROP CONSTRAINT IF EXISTS inspection_records_inspector_id_fkey;
ALTER TABLE IF EXISTS ONLY public.final_settlements DROP CONSTRAINT IF EXISTS final_settlements_project_id_fkey;
ALTER TABLE IF EXISTS ONLY public.final_settlements DROP CONSTRAINT IF EXISTS final_settlements_approved_by_fkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_value DROP CONSTRAINT IF EXISTS dynamic_attr_value_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_value DROP CONSTRAINT IF EXISTS dynamic_attr_value_creator_id_fkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_def DROP CONSTRAINT IF EXISTS dynamic_attr_def_table_id_fkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_def DROP CONSTRAINT IF EXISTS dynamic_attr_def_component_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.device_model_components DROP CONSTRAINT IF EXISTS device_model_components_device_model_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_teams DROP CONSTRAINT IF EXISTS construction_teams_org_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_technical_manager_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_section_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_safety_manager_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_quality_manager_id_fkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_project_manager_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_operator_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_old_component_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_new_component_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_component_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_applications DROP CONSTRAINT IF EXISTS component_replacement_applications_suggested_template_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_applications DROP CONSTRAINT IF EXISTS component_replacement_applications_component_instance_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_applications DROP CONSTRAINT IF EXISTS component_replacement_applications_applicant_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_instances DROP CONSTRAINT IF EXISTS component_instances_template_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_instances DROP CONSTRAINT IF EXISTS component_instances_point_id_fkey;
ALTER TABLE IF EXISTS ONLY public.component_instances DROP CONSTRAINT IF EXISTS component_instances_component_type_id_fkey;
ALTER TABLE IF EXISTS ONLY public.companies DROP CONSTRAINT IF EXISTS companies_type_id_fkey;
DROP TRIGGER IF EXISTS trg_template_insert ON public.attribute_templates;
DROP TRIGGER IF EXISTS trg_subtask_update_point_status ON public.subtask_instances;
DROP TRIGGER IF EXISTS trg_component_insert ON public.component_types;
DROP INDEX IF EXISTS public.idx_users_status;
DROP INDEX IF EXISTS public.idx_users_company;
DROP INDEX IF EXISTS public.idx_user_work_areas_work_area;
DROP INDEX IF EXISTS public.idx_user_work_areas_user;
DROP INDEX IF EXISTS public.idx_user_role_requests_user_id;
DROP INDEX IF EXISTS public.idx_user_role_requests_status;
DROP INDEX IF EXISTS public.idx_subtask_instances_status;
DROP INDEX IF EXISTS public.idx_subtask_instances_point;
DROP INDEX IF EXISTS public.idx_subtask_audit_subtask;
DROP INDEX IF EXISTS public.idx_subtask_audit_point;
DROP INDEX IF EXISTS public.idx_sections_status;
DROP INDEX IF EXISTS public.idx_sections_project;
DROP INDEX IF EXISTS public.idx_projects_status;
DROP INDEX IF EXISTS public.idx_projects_manager;
DROP INDEX IF EXISTS public.idx_projects_company;
DROP INDEX IF EXISTS public.idx_points_work_area;
DROP INDEX IF EXISTS public.idx_points_status;
DROP INDEX IF EXISTS public.idx_points_section;
DROP INDEX IF EXISTS public.idx_points_geom;
DROP INDEX IF EXISTS public.idx_operation_logs_user;
DROP INDEX IF EXISTS public.idx_operation_logs_created;
DROP INDEX IF EXISTS public.idx_notifications_user;
DROP INDEX IF EXISTS public.idx_notifications_is_read;
DROP INDEX IF EXISTS public.idx_dynamic_attr_value_subtask;
DROP INDEX IF EXISTS public.idx_dynamic_attr_value_status;
DROP INDEX IF EXISTS public.idx_dynamic_attr_value_component;
DROP INDEX IF EXISTS public.idx_component_instances_template;
DROP INDEX IF EXISTS public.idx_component_instances_point;
ALTER TABLE IF EXISTS ONLY public.work_areas DROP CONSTRAINT IF EXISTS work_areas_pkey;
ALTER TABLE IF EXISTS ONLY public.work_areas DROP CONSTRAINT IF EXISTS work_areas_company_id_work_area_code_key;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_username_key;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.user_work_areas DROP CONSTRAINT IF EXISTS user_work_areas_user_id_work_area_id_key;
ALTER TABLE IF EXISTS ONLY public.user_work_areas DROP CONSTRAINT IF EXISTS user_work_areas_pkey;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_user_id_role_id_key;
ALTER TABLE IF EXISTS ONLY public.user_roles DROP CONSTRAINT IF EXISTS user_roles_pkey;
ALTER TABLE IF EXISTS ONLY public.user_role_requests DROP CONSTRAINT IF EXISTS user_role_requests_user_id_role_id_key;
ALTER TABLE IF EXISTS ONLY public.user_role_requests DROP CONSTRAINT IF EXISTS user_role_requests_pkey;
ALTER TABLE IF EXISTS ONLY public.user_online_logs DROP CONSTRAINT IF EXISTS user_online_logs_pkey;
ALTER TABLE IF EXISTS ONLY public.team_members DROP CONSTRAINT IF EXISTS team_members_team_id_user_id_key;
ALTER TABLE IF EXISTS ONLY public.team_members DROP CONSTRAINT IF EXISTS team_members_pkey;
ALTER TABLE IF EXISTS ONLY public.task_assignments DROP CONSTRAINT IF EXISTS task_assignments_pkey;
ALTER TABLE IF EXISTS ONLY public.task_assignment_users DROP CONSTRAINT IF EXISTS task_assignment_users_task_id_user_id_key;
ALTER TABLE IF EXISTS ONLY public.task_assignment_users DROP CONSTRAINT IF EXISTS task_assignment_users_pkey;
ALTER TABLE IF EXISTS ONLY public.table_structure_registry DROP CONSTRAINT IF EXISTS table_structure_registry_table_name_key;
ALTER TABLE IF EXISTS ONLY public.table_structure_registry DROP CONSTRAINT IF EXISTS table_structure_registry_pkey;
ALTER TABLE IF EXISTS ONLY public.system_configs DROP CONSTRAINT IF EXISTS system_configs_pkey;
ALTER TABLE IF EXISTS ONLY public.system_configs DROP CONSTRAINT IF EXISTS system_configs_config_key_key;
ALTER TABLE IF EXISTS ONLY public.supervision_weekly_reports DROP CONSTRAINT IF EXISTS supervision_weekly_reports_pkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_audits DROP CONSTRAINT IF EXISTS subtask_scrap_audits_pkey;
ALTER TABLE IF EXISTS ONLY public.subtask_scrap_applications DROP CONSTRAINT IF EXISTS subtask_scrap_applications_pkey;
ALTER TABLE IF EXISTS ONLY public.subtask_instances DROP CONSTRAINT IF EXISTS subtask_instances_point_id_subtask_id_key;
ALTER TABLE IF EXISTS ONLY public.subtask_instances DROP CONSTRAINT IF EXISTS subtask_instances_pkey;
ALTER TABLE IF EXISTS ONLY public.subtask_definitions DROP CONSTRAINT IF EXISTS subtask_definitions_subtask_code_key;
ALTER TABLE IF EXISTS ONLY public.subtask_definitions DROP CONSTRAINT IF EXISTS subtask_definitions_pkey;
ALTER TABLE IF EXISTS ONLY public.subtask_audit_records DROP CONSTRAINT IF EXISTS subtask_audit_records_pkey;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_role_name_key;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_role_code_key;
ALTER TABLE IF EXISTS ONLY public.roles DROP CONSTRAINT IF EXISTS roles_pkey;
ALTER TABLE IF EXISTS ONLY public.role_permissions DROP CONSTRAINT IF EXISTS role_permissions_role_id_permission_id_key;
ALTER TABLE IF EXISTS ONLY public.role_permissions DROP CONSTRAINT IF EXISTS role_permissions_pkey;
ALTER TABLE IF EXISTS ONLY public.risk_rectification_items DROP CONSTRAINT IF EXISTS risk_rectification_items_pkey;
ALTER TABLE IF EXISTS ONLY public.projects DROP CONSTRAINT IF EXISTS projects_pkey;
ALTER TABLE IF EXISTS ONLY public.project_sections DROP CONSTRAINT IF EXISTS project_sections_pkey;
ALTER TABLE IF EXISTS ONLY public.project_quantities DROP CONSTRAINT IF EXISTS project_quantities_pkey;
ALTER TABLE IF EXISTS ONLY public.project_authorizations DROP CONSTRAINT IF EXISTS project_authorizations_pkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_audits DROP CONSTRAINT IF EXISTS progress_payment_audits_pkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_applications DROP CONSTRAINT IF EXISTS progress_payment_applications_pkey;
ALTER TABLE IF EXISTS ONLY public.progress_payment_applications DROP CONSTRAINT IF EXISTS progress_payment_applications_application_number_key;
ALTER TABLE IF EXISTS ONLY public.points DROP CONSTRAINT IF EXISTS points_point_code_key;
ALTER TABLE IF EXISTS ONLY public.points DROP CONSTRAINT IF EXISTS points_pkey;
ALTER TABLE IF EXISTS ONLY public.point_status DROP CONSTRAINT IF EXISTS point_status_pkey;
ALTER TABLE IF EXISTS ONLY public.permissions DROP CONSTRAINT IF EXISTS permissions_pkey;
ALTER TABLE IF EXISTS ONLY public.permissions DROP CONSTRAINT IF EXISTS permissions_permission_name_key;
ALTER TABLE IF EXISTS ONLY public.permissions DROP CONSTRAINT IF EXISTS permissions_permission_code_key;
ALTER TABLE IF EXISTS ONLY public.page_access_logs DROP CONSTRAINT IF EXISTS page_access_logs_pkey;
ALTER TABLE IF EXISTS ONLY public.operation_logs DROP CONSTRAINT IF EXISTS operation_logs_pkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_pkey;
ALTER TABLE IF EXISTS ONLY public.inspection_records DROP CONSTRAINT IF EXISTS inspection_records_pkey;
ALTER TABLE IF EXISTS ONLY public.final_settlements DROP CONSTRAINT IF EXISTS final_settlements_settlement_number_key;
ALTER TABLE IF EXISTS ONLY public.final_settlements DROP CONSTRAINT IF EXISTS final_settlements_pkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_value DROP CONSTRAINT IF EXISTS dynamic_attr_value_pkey;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_def DROP CONSTRAINT IF EXISTS dynamic_attr_def_table_id_column_name_key;
ALTER TABLE IF EXISTS ONLY public.dynamic_attr_def DROP CONSTRAINT IF EXISTS dynamic_attr_def_pkey;
ALTER TABLE IF EXISTS ONLY public.device_models DROP CONSTRAINT IF EXISTS device_models_pkey;
ALTER TABLE IF EXISTS ONLY public.device_models DROP CONSTRAINT IF EXISTS device_models_model_name_key;
ALTER TABLE IF EXISTS ONLY public.device_model_components DROP CONSTRAINT IF EXISTS device_model_components_pkey;
ALTER TABLE IF EXISTS ONLY public.device_model_components DROP CONSTRAINT IF EXISTS device_model_components_device_model_id_component_id_key;
ALTER TABLE IF EXISTS ONLY public.construction_teams DROP CONSTRAINT IF EXISTS construction_teams_pkey;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_section_id_key;
ALTER TABLE IF EXISTS ONLY public.construction_orgs DROP CONSTRAINT IF EXISTS construction_orgs_pkey;
ALTER TABLE IF EXISTS ONLY public.component_types DROP CONSTRAINT IF EXISTS component_types_pkey;
ALTER TABLE IF EXISTS ONLY public.component_types DROP CONSTRAINT IF EXISTS component_types_code_key;
ALTER TABLE IF EXISTS ONLY public.component_replacement_history DROP CONSTRAINT IF EXISTS component_replacement_history_pkey;
ALTER TABLE IF EXISTS ONLY public.component_replacement_applications DROP CONSTRAINT IF EXISTS component_replacement_applications_pkey;
ALTER TABLE IF EXISTS ONLY public.component_instances DROP CONSTRAINT IF EXISTS component_instances_pkey;
ALTER TABLE IF EXISTS ONLY public.company_types DROP CONSTRAINT IF EXISTS company_types_type_name_key;
ALTER TABLE IF EXISTS ONLY public.company_types DROP CONSTRAINT IF EXISTS company_types_pkey;
ALTER TABLE IF EXISTS ONLY public.companies DROP CONSTRAINT IF EXISTS companies_pkey;
ALTER TABLE IF EXISTS ONLY public.companies DROP CONSTRAINT IF EXISTS companies_company_name_key;
ALTER TABLE IF EXISTS ONLY public.backup_records DROP CONSTRAINT IF EXISTS backup_records_pkey;
ALTER TABLE IF EXISTS ONLY public.attribute_templates DROP CONSTRAINT IF EXISTS attribute_templates_pkey;
ALTER TABLE IF EXISTS public.work_areas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.user_work_areas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.user_roles ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.user_role_requests ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.user_online_logs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.team_members ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.task_assignments ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.task_assignment_users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.table_structure_registry ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.system_configs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.supervision_weekly_reports ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.subtask_scrap_audits ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.subtask_scrap_applications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.subtask_instances ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.subtask_definitions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.subtask_audit_records ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.roles ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.role_permissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.risk_rectification_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.projects ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.project_sections ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.project_quantities ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.project_authorizations ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.progress_payment_audits ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.progress_payment_applications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.points ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.permissions ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.page_access_logs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.operation_logs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.notifications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.inspection_records ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.final_settlements ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.dynamic_attr_value ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.dynamic_attr_def ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.device_models ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.device_model_components ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.construction_teams ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.construction_orgs ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.component_types ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.component_replacement_history ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.component_replacement_applications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.component_instances ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.company_types ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.companies ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.backup_records ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.attribute_templates ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.work_areas_id_seq;
DROP TABLE IF EXISTS public.work_areas;
DROP VIEW IF EXISTS public.v_user_roles;
DROP VIEW IF EXISTS public.v_project_sections;
DROP VIEW IF EXISTS public.v_point_subtasks;
DROP SEQUENCE IF EXISTS public.users_id_seq;
DROP TABLE IF EXISTS public.users;
DROP SEQUENCE IF EXISTS public.user_work_areas_id_seq;
DROP TABLE IF EXISTS public.user_work_areas;
DROP SEQUENCE IF EXISTS public.user_roles_id_seq;
DROP TABLE IF EXISTS public.user_roles;
DROP SEQUENCE IF EXISTS public.user_role_requests_id_seq;
DROP TABLE IF EXISTS public.user_role_requests;
DROP SEQUENCE IF EXISTS public.user_online_logs_id_seq;
DROP TABLE IF EXISTS public.user_online_logs;
DROP SEQUENCE IF EXISTS public.team_members_id_seq;
DROP TABLE IF EXISTS public.team_members;
DROP SEQUENCE IF EXISTS public.task_assignments_id_seq;
DROP TABLE IF EXISTS public.task_assignments;
DROP SEQUENCE IF EXISTS public.task_assignment_users_id_seq;
DROP TABLE IF EXISTS public.task_assignment_users;
DROP SEQUENCE IF EXISTS public.table_structure_registry_id_seq;
DROP TABLE IF EXISTS public.table_structure_registry;
DROP SEQUENCE IF EXISTS public.system_configs_id_seq;
DROP TABLE IF EXISTS public.system_configs;
DROP SEQUENCE IF EXISTS public.supervision_weekly_reports_id_seq;
DROP TABLE IF EXISTS public.supervision_weekly_reports;
DROP SEQUENCE IF EXISTS public.subtask_scrap_audits_id_seq;
DROP TABLE IF EXISTS public.subtask_scrap_audits;
DROP SEQUENCE IF EXISTS public.subtask_scrap_applications_id_seq;
DROP TABLE IF EXISTS public.subtask_scrap_applications;
DROP SEQUENCE IF EXISTS public.subtask_instances_id_seq;
DROP TABLE IF EXISTS public.subtask_instances;
DROP SEQUENCE IF EXISTS public.subtask_definitions_id_seq;
DROP TABLE IF EXISTS public.subtask_definitions;
DROP SEQUENCE IF EXISTS public.subtask_audit_records_id_seq;
DROP TABLE IF EXISTS public.subtask_audit_records;
DROP SEQUENCE IF EXISTS public.roles_id_seq;
DROP TABLE IF EXISTS public.roles;
DROP SEQUENCE IF EXISTS public.role_permissions_id_seq;
DROP TABLE IF EXISTS public.role_permissions;
DROP SEQUENCE IF EXISTS public.risk_rectification_items_id_seq;
DROP TABLE IF EXISTS public.risk_rectification_items;
DROP SEQUENCE IF EXISTS public.projects_id_seq;
DROP TABLE IF EXISTS public.projects;
DROP SEQUENCE IF EXISTS public.project_sections_id_seq;
DROP TABLE IF EXISTS public.project_sections;
DROP SEQUENCE IF EXISTS public.project_quantities_id_seq;
DROP TABLE IF EXISTS public.project_quantities;
DROP SEQUENCE IF EXISTS public.project_authorizations_id_seq;
DROP TABLE IF EXISTS public.project_authorizations;
DROP SEQUENCE IF EXISTS public.progress_payment_audits_id_seq;
DROP TABLE IF EXISTS public.progress_payment_audits;
DROP SEQUENCE IF EXISTS public.progress_payment_applications_id_seq;
DROP TABLE IF EXISTS public.progress_payment_applications;
DROP SEQUENCE IF EXISTS public.points_id_seq;
DROP TABLE IF EXISTS public.points;
DROP TABLE IF EXISTS public.point_status;
DROP SEQUENCE IF EXISTS public.permissions_id_seq;
DROP TABLE IF EXISTS public.permissions;
DROP SEQUENCE IF EXISTS public.page_access_logs_id_seq;
DROP TABLE IF EXISTS public.page_access_logs;
DROP SEQUENCE IF EXISTS public.operation_logs_id_seq;
DROP TABLE IF EXISTS public.operation_logs;
DROP SEQUENCE IF EXISTS public.notifications_id_seq;
DROP TABLE IF EXISTS public.notifications;
DROP SEQUENCE IF EXISTS public.inspection_records_id_seq;
DROP TABLE IF EXISTS public.inspection_records;
DROP SEQUENCE IF EXISTS public.final_settlements_id_seq;
DROP TABLE IF EXISTS public.final_settlements;
DROP SEQUENCE IF EXISTS public.dynamic_attr_value_id_seq;
DROP TABLE IF EXISTS public.dynamic_attr_value;
DROP SEQUENCE IF EXISTS public.dynamic_attr_def_id_seq;
DROP TABLE IF EXISTS public.dynamic_attr_def;
DROP SEQUENCE IF EXISTS public.device_models_id_seq;
DROP TABLE IF EXISTS public.device_models;
DROP SEQUENCE IF EXISTS public.device_model_components_id_seq;
DROP TABLE IF EXISTS public.device_model_components;
DROP SEQUENCE IF EXISTS public.construction_teams_id_seq;
DROP TABLE IF EXISTS public.construction_teams;
DROP SEQUENCE IF EXISTS public.construction_orgs_id_seq;
DROP TABLE IF EXISTS public.construction_orgs;
DROP SEQUENCE IF EXISTS public.component_types_id_seq;
DROP TABLE IF EXISTS public.component_types;
DROP SEQUENCE IF EXISTS public.component_replacement_history_id_seq;
DROP TABLE IF EXISTS public.component_replacement_history;
DROP SEQUENCE IF EXISTS public.component_replacement_applications_id_seq;
DROP TABLE IF EXISTS public.component_replacement_applications;
DROP SEQUENCE IF EXISTS public.component_instances_id_seq;
DROP TABLE IF EXISTS public.component_instances;
DROP SEQUENCE IF EXISTS public.company_types_id_seq;
DROP TABLE IF EXISTS public.company_types;
DROP SEQUENCE IF EXISTS public.companies_id_seq;
DROP TABLE IF EXISTS public.companies;
DROP SEQUENCE IF EXISTS public.backup_records_id_seq;
DROP TABLE IF EXISTS public.backup_records;
DROP SEQUENCE IF EXISTS public.attribute_templates_id_seq;
DROP TABLE IF EXISTS public.attribute_templates;
DROP FUNCTION IF EXISTS public.update_point_status_from_subtasks();
DROP FUNCTION IF EXISTS public.on_template_insert();
DROP FUNCTION IF EXISTS public.on_component_insert();
DROP FUNCTION IF EXISTS public.create_template_table(p_template_id bigint);
DROP FUNCTION IF EXISTS public.create_component_tables(p_component_id bigint);
DROP EXTENSION IF EXISTS "uuid-ossp";
DROP EXTENSION IF EXISTS postgis;
--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: create_component_tables(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.create_component_tables(p_component_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    attr_def_table TEXT;
    attr_value_table TEXT;
    def_table_id BIGINT;
    value_table_id BIGINT;
BEGIN
    attr_def_table := format('component_attr_def_%s', p_component_id);
    attr_value_table := format('component_attr_value_%s', p_component_id);
    
    -- 创建属性定义表
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I (
            attr_id BIGSERIAL PRIMARY KEY,
            column_name VARCHAR(100) NOT NULL,
            column_type VARCHAR(20) NOT NULL,
            max_length INT,
            attribute_name VARCHAR(100) NOT NULL,
            attribute_order INT NOT NULL DEFAULT 10,
            unit VARCHAR(20),
            audit_standard VARCHAR(500),
            is_required BOOLEAN DEFAULT false,
            default_value VARCHAR(255),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE(column_name)
        )', attr_def_table);
    
    -- 创建属性值表
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I (
            value_id BIGSERIAL PRIMARY KEY,
            component_instance_id BIGINT NOT NULL REFERENCES component_instances(id),
            point_id BIGINT NOT NULL REFERENCES points(id),
            attr_id BIGINT NOT NULL,
            attr_value TEXT,
            creator_id BIGINT NOT NULL REFERENCES users(id),
            status INT DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )', attr_value_table);
    
    -- 注册到表结构注册表
    INSERT INTO table_structure_registry (component_type_id, table_name, table_type, table_comment)
    VALUES 
        (p_component_id, attr_def_table, 'attr_def', format('零部件%s属性定义表', p_component_id)),
        (p_component_id, attr_value_table, 'attr_value', format('零部件%s属性值表', p_component_id))
    ON CONFLICT (table_name) DO NOTHING;
END;
$$;


ALTER FUNCTION public.create_component_tables(p_component_id bigint) OWNER TO postgres;

--
-- Name: create_template_table(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.create_template_table(p_template_id bigint) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    template_value_table TEXT;
BEGIN
    template_value_table := format('component_template_value_%s', p_template_id);
    
    -- 创建模板属性值表
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I (
            value_id BIGSERIAL PRIMARY KEY,
            template_id BIGINT NOT NULL REFERENCES attribute_templates(id),
            attr_id BIGINT NOT NULL,
            attr_value TEXT,
            creator_id BIGINT NOT NULL REFERENCES users(id),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )', template_value_table);
    
    -- 注册到表结构注册表
    INSERT INTO table_structure_registry (template_id, table_name, table_type, table_comment)
    VALUES (p_template_id, template_value_table, 'template_value', format('模板%s属性值表', p_template_id))
    ON CONFLICT (table_name) DO NOTHING;
END;
$$;


ALTER FUNCTION public.create_template_table(p_template_id bigint) OWNER TO postgres;

--
-- Name: on_component_insert(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.on_component_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM create_component_tables(NEW.id);
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.on_component_insert() OWNER TO postgres;

--
-- Name: on_template_insert(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.on_template_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    PERFORM create_template_table(NEW.id);
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.on_template_insert() OWNER TO postgres;

--
-- Name: update_point_status_from_subtasks(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_point_status_from_subtasks() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- 根据子任务状态更新点位状态
    UPDATE point_status ps
    SET 
        status = CASE
            WHEN EXISTS (
                SELECT 1 FROM subtask_instances sti 
                WHERE sti.point_id = ps.point_id 
                AND sti.status = '已报废'
            ) THEN '已报废'
            WHEN EXISTS (
                SELECT 1 FROM subtask_instances sti 
                WHERE sti.point_id = ps.point_id 
                AND sti.status = '审核通过'
                HAVING COUNT(*) = (SELECT COUNT(*) FROM subtask_instances WHERE point_id = ps.point_id)
            ) THEN '审核通过'
            WHEN EXISTS (
                SELECT 1 FROM subtask_instances sti 
                WHERE sti.point_id = ps.point_id 
                AND sti.status = '整改中'
            ) THEN '整改中'
            WHEN EXISTS (
                SELECT 1 FROM subtask_instances sti 
                WHERE sti.point_id = ps.point_id 
                AND sti.status = '待审核'
            ) THEN '待审核'
            WHEN EXISTS (
                SELECT 1 FROM subtask_instances sti 
                WHERE sti.point_id = ps.point_id 
                AND sti.status NOT IN ('未开始')
            ) THEN '施工中'
            ELSE '未开工'
        END,
        subtask_stats = (
            SELECT jsonb_object_agg(status, cnt)
            FROM (
                SELECT status, COUNT(*) as cnt
                FROM subtask_instances
                WHERE point_id = ps.point_id
                GROUP BY status
            ) t
        ),
        last_updated = CURRENT_TIMESTAMP
    WHERE ps.point_id = NEW.point_id;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_point_status_from_subtasks() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: attribute_templates; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.attribute_templates (
    id bigint NOT NULL,
    template_name character varying(100) NOT NULL,
    component_type_id bigint NOT NULL,
    table_id bigint NOT NULL,
    is_default boolean DEFAULT false,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.attribute_templates OWNER TO postgres;

--
-- Name: TABLE attribute_templates; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.attribute_templates IS '属性模板表';


--
-- Name: COLUMN attribute_templates.table_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.attribute_templates.table_id IS '关联的表结构 ID（对应属性定义）';


--
-- Name: attribute_templates_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.attribute_templates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.attribute_templates_id_seq OWNER TO postgres;

--
-- Name: attribute_templates_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.attribute_templates_id_seq OWNED BY public.attribute_templates.id;


--
-- Name: backup_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.backup_records (
    id bigint NOT NULL,
    backup_type character varying(20) NOT NULL,
    backup_path character varying(500),
    backup_size bigint,
    status character varying(20) DEFAULT 'pending'::character varying,
    started_at timestamp without time zone,
    completed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.backup_records OWNER TO postgres;

--
-- Name: TABLE backup_records; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.backup_records IS '备份记录表';


--
-- Name: COLUMN backup_records.backup_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.backup_records.backup_type IS '备份类型（full:全量 incremental:增量）';


--
-- Name: backup_records_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.backup_records_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.backup_records_id_seq OWNER TO postgres;

--
-- Name: backup_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.backup_records_id_seq OWNED BY public.backup_records.id;


--
-- Name: companies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.companies (
    id bigint NOT NULL,
    company_name character varying(100) NOT NULL,
    type_id bigint NOT NULL,
    unified_social_credit_code character varying(50),
    contact_person character varying(50),
    contact_phone character varying(20),
    contact_email character varying(100),
    address text,
    status character varying(20) DEFAULT 'active'::character varying,
    description text,
    is_system_protected boolean DEFAULT false,
    created_by bigint,
    updated_by bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    allow_anonymous_register boolean DEFAULT false
);


ALTER TABLE public.companies OWNER TO postgres;

--
-- Name: TABLE companies; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.companies IS '公司表';


--
-- Name: COLUMN companies.type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.companies.type_id IS '公司类型 ID（外键关联 company_types）';


--
-- Name: COLUMN companies.unified_social_credit_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.companies.unified_social_credit_code IS '统一社会信用代码';


--
-- Name: COLUMN companies.is_system_protected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.companies.is_system_protected IS '是否系统保护（软件所有者公司不可删除）';


--
-- Name: COLUMN companies.allow_anonymous_register; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.companies.allow_anonymous_register IS '是否允许匿名注册';


--
-- Name: companies_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.companies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.companies_id_seq OWNER TO postgres;

--
-- Name: companies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.companies_id_seq OWNED BY public.companies.id;


--
-- Name: company_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.company_types (
    id bigint NOT NULL,
    type_name character varying(100) NOT NULL,
    description text,
    is_system_protected boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.company_types OWNER TO postgres;

--
-- Name: TABLE company_types; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.company_types IS '公司类型表';


--
-- Name: COLUMN company_types.type_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.company_types.type_name IS '类型名称（甲方公司/乙方公司/监理公司/软件所有者公司）';


--
-- Name: COLUMN company_types.is_system_protected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.company_types.is_system_protected IS '是否系统保护（软件所有者公司不可删除）';


--
-- Name: company_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.company_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.company_types_id_seq OWNER TO postgres;

--
-- Name: company_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.company_types_id_seq OWNED BY public.company_types.id;


--
-- Name: component_instances; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component_instances (
    id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    point_id bigint NOT NULL,
    instance_name character varying(100),
    instance_order integer DEFAULT 10 NOT NULL,
    template_id bigint,
    status character varying(20) DEFAULT 'normal'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.component_instances OWNER TO postgres;

--
-- Name: TABLE component_instances; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.component_instances IS '零部件实例表';


--
-- Name: COLUMN component_instances.template_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.component_instances.template_id IS '关联的属性模板 ID';


--
-- Name: COLUMN component_instances.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.component_instances.status IS '状态（normal:正常使用 replaced:已更换 damaged:损坏）';


--
-- Name: component_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.component_instances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.component_instances_id_seq OWNER TO postgres;

--
-- Name: component_instances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.component_instances_id_seq OWNED BY public.component_instances.id;


--
-- Name: component_replacement_applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component_replacement_applications (
    id bigint NOT NULL,
    component_instance_id bigint NOT NULL,
    applicant_id bigint NOT NULL,
    replacement_reason text NOT NULL,
    damage_description text,
    expected_date date,
    suggested_template_id bigint,
    status character varying(50) DEFAULT 'pending'::character varying,
    team_leader_audit_id bigint,
    manager_audit_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.component_replacement_applications OWNER TO postgres;

--
-- Name: TABLE component_replacement_applications; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.component_replacement_applications IS '零部件更换申请表';


--
-- Name: component_replacement_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.component_replacement_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.component_replacement_applications_id_seq OWNER TO postgres;

--
-- Name: component_replacement_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.component_replacement_applications_id_seq OWNED BY public.component_replacement_applications.id;


--
-- Name: component_replacement_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component_replacement_history (
    id bigint NOT NULL,
    point_id bigint NOT NULL,
    component_type_id bigint NOT NULL,
    old_component_id bigint,
    new_component_id bigint,
    replacement_reason text,
    replacement_date timestamp without time zone,
    operator_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.component_replacement_history OWNER TO postgres;

--
-- Name: TABLE component_replacement_history; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.component_replacement_history IS '零部件更换历史表';


--
-- Name: component_replacement_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.component_replacement_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.component_replacement_history_id_seq OWNER TO postgres;

--
-- Name: component_replacement_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.component_replacement_history_id_seq OWNED BY public.component_replacement_history.id;


--
-- Name: component_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.component_types (
    id bigint NOT NULL,
    component_name character varying(100) NOT NULL,
    code character varying(50) NOT NULL,
    description text,
    display_order integer DEFAULT 10 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.component_types OWNER TO postgres;

--
-- Name: TABLE component_types; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.component_types IS '零部件种类表';


--
-- Name: component_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.component_types_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.component_types_id_seq OWNER TO postgres;

--
-- Name: component_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.component_types_id_seq OWNED BY public.component_types.id;


--
-- Name: construction_orgs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.construction_orgs (
    id bigint NOT NULL,
    section_id bigint NOT NULL,
    project_manager_id bigint NOT NULL,
    technical_manager_id bigint NOT NULL,
    safety_manager_id bigint NOT NULL,
    quality_manager_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.construction_orgs OWNER TO postgres;

--
-- Name: TABLE construction_orgs; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.construction_orgs IS '施工组织表';


--
-- Name: COLUMN construction_orgs.section_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.construction_orgs.section_id IS '所属标段 ID（即乙方项目 ID）';


--
-- Name: construction_orgs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.construction_orgs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.construction_orgs_id_seq OWNER TO postgres;

--
-- Name: construction_orgs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.construction_orgs_id_seq OWNED BY public.construction_orgs.id;


--
-- Name: construction_teams; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.construction_teams (
    id bigint NOT NULL,
    team_name character varying(100) NOT NULL,
    org_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.construction_teams OWNER TO postgres;

--
-- Name: TABLE construction_teams; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.construction_teams IS '施工队表';


--
-- Name: COLUMN construction_teams.org_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.construction_teams.org_id IS '所属施工组织 ID';


--
-- Name: construction_teams_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.construction_teams_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.construction_teams_id_seq OWNER TO postgres;

--
-- Name: construction_teams_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.construction_teams_id_seq OWNED BY public.construction_teams.id;


--
-- Name: device_model_components; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.device_model_components (
    id bigint NOT NULL,
    device_model_id bigint NOT NULL,
    component_id bigint NOT NULL,
    quantity integer DEFAULT 1 NOT NULL,
    display_order integer DEFAULT 10 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.device_model_components OWNER TO postgres;

--
-- Name: TABLE device_model_components; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.device_model_components IS '设备模型零部件关系表';


--
-- Name: COLUMN device_model_components.component_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_model_components.component_id IS '零部件种类 ID';


--
-- Name: COLUMN device_model_components.quantity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_model_components.quantity IS '该设备模型中包含的此零部件种类的数量';


--
-- Name: COLUMN device_model_components.display_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_model_components.display_order IS '显示顺序';


--
-- Name: device_model_components_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.device_model_components_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.device_model_components_id_seq OWNER TO postgres;

--
-- Name: device_model_components_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.device_model_components_id_seq OWNED BY public.device_model_components.id;


--
-- Name: device_models; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.device_models (
    id bigint NOT NULL,
    model_name character varying(100) NOT NULL,
    description text,
    applicable_project_type character varying(50) NOT NULL,
    applicable_project_sub_type character varying(50) NOT NULL,
    applicable_construction_type character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.device_models OWNER TO postgres;

--
-- Name: TABLE device_models; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.device_models IS '设备模型表';


--
-- Name: COLUMN device_models.applicable_project_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_models.applicable_project_type IS '适用项目大类';


--
-- Name: COLUMN device_models.applicable_project_sub_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_models.applicable_project_sub_type IS '适用项目小类';


--
-- Name: COLUMN device_models.applicable_construction_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.device_models.applicable_construction_type IS '适用施工类型';


--
-- Name: device_models_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.device_models_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.device_models_id_seq OWNER TO postgres;

--
-- Name: device_models_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.device_models_id_seq OWNED BY public.device_models.id;


--
-- Name: dynamic_attr_def; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dynamic_attr_def (
    id bigint NOT NULL,
    table_id bigint NOT NULL,
    component_type_id bigint,
    subtask_id bigint,
    column_name character varying(100) NOT NULL,
    column_type character varying(20) NOT NULL,
    max_length integer,
    attribute_name character varying(100) NOT NULL,
    attribute_order integer DEFAULT 10 NOT NULL,
    unit character varying(20),
    audit_standard character varying(500),
    is_required boolean DEFAULT false,
    default_value character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.dynamic_attr_def OWNER TO postgres;

--
-- Name: TABLE dynamic_attr_def; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dynamic_attr_def IS '动态属性定义总表（统一管理所有属性定义元数据）';


--
-- Name: COLUMN dynamic_attr_def.column_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dynamic_attr_def.column_name IS '数据库列名（snake_case 格式）';


--
-- Name: COLUMN dynamic_attr_def.column_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dynamic_attr_def.column_type IS '列类型（varchar/number/datetime/text）';


--
-- Name: dynamic_attr_def_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dynamic_attr_def_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.dynamic_attr_def_id_seq OWNER TO postgres;

--
-- Name: dynamic_attr_def_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dynamic_attr_def_id_seq OWNED BY public.dynamic_attr_def.id;


--
-- Name: dynamic_attr_value; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dynamic_attr_value (
    id bigint NOT NULL,
    component_instance_id bigint,
    subtask_instance_id bigint,
    point_id bigint,
    template_id bigint,
    table_name character varying(100) NOT NULL,
    record_id bigint NOT NULL,
    attribute_data jsonb,
    is_from_template boolean DEFAULT false,
    status integer DEFAULT 0,
    creator_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.dynamic_attr_value OWNER TO postgres;

--
-- Name: TABLE dynamic_attr_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dynamic_attr_value IS '动态属性值总表（统一管理所有差异值）';


--
-- Name: COLUMN dynamic_attr_value.attribute_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dynamic_attr_value.attribute_data IS '差异属性（JSON 格式）';


--
-- Name: COLUMN dynamic_attr_value.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dynamic_attr_value.status IS '状态（0:待提交 1:待审核 2:已通过 3:已驳回 4:已作废）';


--
-- Name: dynamic_attr_value_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dynamic_attr_value_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.dynamic_attr_value_id_seq OWNER TO postgres;

--
-- Name: dynamic_attr_value_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dynamic_attr_value_id_seq OWNED BY public.dynamic_attr_value.id;


--
-- Name: final_settlements; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.final_settlements (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    settlement_number character varying(50),
    contract_amount numeric(18,2),
    total_completed_quantity numeric(10,2),
    total_approved_amount numeric(18,2),
    adjustment_amount numeric(18,2),
    final_amount numeric(18,2),
    paid_amount numeric(18,2),
    balance_amount numeric(18,2),
    settlement_report_url character varying(500),
    audit_report_url character varying(500),
    status character varying(50) DEFAULT 'pending'::character varying,
    approved_by bigint,
    approved_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.final_settlements OWNER TO postgres;

--
-- Name: TABLE final_settlements; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.final_settlements IS '最终结算表';


--
-- Name: COLUMN final_settlements.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.final_settlements.status IS '状态（pending/under_audit/approved/completed）';


--
-- Name: final_settlements_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.final_settlements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.final_settlements_id_seq OWNER TO postgres;

--
-- Name: final_settlements_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.final_settlements_id_seq OWNED BY public.final_settlements.id;


--
-- Name: inspection_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.inspection_records (
    id bigint NOT NULL,
    point_id bigint NOT NULL,
    inspector_id bigint NOT NULL,
    inspection_type character varying(50),
    inspection_result character varying(20) NOT NULL,
    inspection_comment text,
    reviewer_id bigint,
    review_opinion text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.inspection_records OWNER TO postgres;

--
-- Name: TABLE inspection_records; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.inspection_records IS '巡检记录表';


--
-- Name: COLUMN inspection_records.inspection_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.inspection_records.inspection_type IS '巡检类型（主动巡检/复核）';


--
-- Name: COLUMN inspection_records.inspection_result; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.inspection_records.inspection_result IS '巡检结果（pass:通过 reject:不通过）';


--
-- Name: inspection_records_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.inspection_records_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.inspection_records_id_seq OWNER TO postgres;

--
-- Name: inspection_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.inspection_records_id_seq OWNED BY public.inspection_records.id;


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    notification_type character varying(50),
    title character varying(200),
    content text,
    is_read boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: TABLE notifications; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.notifications IS '通知表';


--
-- Name: COLUMN notifications.notification_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.notifications.notification_type IS '通知类型（email/sms/system）';


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifications_id_seq OWNER TO postgres;

--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: operation_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.operation_logs (
    id bigint NOT NULL,
    user_id bigint,
    operation_type character varying(50),
    operation_content text,
    operation_result character varying(20),
    ip_address character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.operation_logs OWNER TO postgres;

--
-- Name: TABLE operation_logs; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.operation_logs IS '操作日志表';


--
-- Name: operation_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.operation_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.operation_logs_id_seq OWNER TO postgres;

--
-- Name: operation_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.operation_logs_id_seq OWNED BY public.operation_logs.id;


--
-- Name: page_access_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.page_access_logs (
    id bigint NOT NULL,
    user_id bigint,
    page_url character varying(500),
    page_name character varying(100),
    accessed_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.page_access_logs OWNER TO postgres;

--
-- Name: TABLE page_access_logs; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.page_access_logs IS '页面访问日志表';


--
-- Name: page_access_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.page_access_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.page_access_logs_id_seq OWNER TO postgres;

--
-- Name: page_access_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.page_access_logs_id_seq OWNED BY public.page_access_logs.id;


--
-- Name: permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permissions (
    id bigint NOT NULL,
    permission_name character varying(50) NOT NULL,
    permission_code character varying(50) NOT NULL,
    permission_description character varying(200),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.permissions OWNER TO postgres;

--
-- Name: TABLE permissions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.permissions IS '权限表';


--
-- Name: permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.permissions_id_seq OWNER TO postgres;

--
-- Name: permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.permissions_id_seq OWNED BY public.permissions.id;


--
-- Name: point_status; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.point_status (
    point_id bigint NOT NULL,
    status character varying(50) DEFAULT '未开工'::character varying NOT NULL,
    subtask_stats jsonb,
    last_updated timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.point_status OWNER TO postgres;

--
-- Name: TABLE point_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.point_status IS '点位状态表（物化视图）';


--
-- Name: COLUMN point_status.subtask_stats; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.point_status.subtask_stats IS '子任务统计（JSON 格式）';


--
-- Name: points; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.points (
    id bigint NOT NULL,
    point_name character varying(100),
    point_code character varying(50) NOT NULL,
    section_id bigint NOT NULL,
    work_area_id bigint NOT NULL,
    device_model_id bigint,
    latitude numeric(10,6),
    longitude numeric(10,6),
    geom public.geometry(Point,4326),
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.points OWNER TO postgres;

--
-- Name: TABLE points; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.points IS '点位表';


--
-- Name: COLUMN points.device_model_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.points.device_model_id IS '设备模型 ID';


--
-- Name: COLUMN points.geom; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.points.geom IS 'PostGIS 地理空间坐标';


--
-- Name: COLUMN points.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.points.status IS '点位状态（0:未开工 1:施工中 2:待审核 3:整改中 4:审核通过 5:已报废）';


--
-- Name: points_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.points_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.points_id_seq OWNER TO postgres;

--
-- Name: points_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.points_id_seq OWNED BY public.points.id;


--
-- Name: progress_payment_applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.progress_payment_applications (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    section_id bigint,
    application_number character varying(50),
    application_period_start date NOT NULL,
    application_period_end date NOT NULL,
    total_quantity numeric(10,2),
    unit_price numeric(10,2),
    total_amount numeric(18,2),
    attachment_urls jsonb,
    status character varying(50) DEFAULT 'pending'::character varying,
    contractor_audit_id bigint,
    supervisor_audit_id bigint,
    party_a_audit_id bigint,
    paid_amount numeric(18,2),
    paid_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.progress_payment_applications OWNER TO postgres;

--
-- Name: TABLE progress_payment_applications; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.progress_payment_applications IS '进度款申请表';


--
-- Name: COLUMN progress_payment_applications.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.progress_payment_applications.status IS '状态（pending/contractor_approved/supervisor_approved/party_a_approved/rejected/paid）';


--
-- Name: progress_payment_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.progress_payment_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.progress_payment_applications_id_seq OWNER TO postgres;

--
-- Name: progress_payment_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.progress_payment_applications_id_seq OWNED BY public.progress_payment_applications.id;


--
-- Name: progress_payment_audits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.progress_payment_audits (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    auditor_id bigint NOT NULL,
    auditor_role character varying(50) NOT NULL,
    audit_result character varying(20) NOT NULL,
    audit_opinion text,
    audited_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.progress_payment_audits OWNER TO postgres;

--
-- Name: TABLE progress_payment_audits; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.progress_payment_audits IS '进度款审核表';


--
-- Name: progress_payment_audits_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.progress_payment_audits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.progress_payment_audits_id_seq OWNER TO postgres;

--
-- Name: progress_payment_audits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.progress_payment_audits_id_seq OWNED BY public.progress_payment_audits.id;


--
-- Name: project_authorizations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_authorizations (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    authorized_user_id bigint NOT NULL,
    granted_by_user_id bigint NOT NULL,
    granted_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamp without time zone,
    is_active boolean DEFAULT true
);


ALTER TABLE public.project_authorizations OWNER TO postgres;

--
-- Name: TABLE project_authorizations; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.project_authorizations IS '项目授权表（系统管理员临时操作甲方项目）';


--
-- Name: project_authorizations_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_authorizations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.project_authorizations_id_seq OWNER TO postgres;

--
-- Name: project_authorizations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.project_authorizations_id_seq OWNED BY public.project_authorizations.id;


--
-- Name: project_quantities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_quantities (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    section_id bigint,
    point_id bigint,
    subtask_id bigint NOT NULL,
    subtask_instance_id bigint,
    quantity numeric(10,2),
    unit character varying(20),
    status character varying(50) DEFAULT 'pending'::character varying,
    confirmed_by bigint,
    confirmed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.project_quantities OWNER TO postgres;

--
-- Name: TABLE project_quantities; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.project_quantities IS '工程量统计表';


--
-- Name: COLUMN project_quantities.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.project_quantities.status IS '状态（pending:待确认 confirmed:已确认 rejected:已拒绝）';


--
-- Name: project_quantities_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_quantities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.project_quantities_id_seq OWNER TO postgres;

--
-- Name: project_quantities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.project_quantities_id_seq OWNED BY public.project_quantities.id;


--
-- Name: project_sections; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.project_sections (
    id bigint NOT NULL,
    section_name character varying(100) NOT NULL,
    description text,
    project_id bigint NOT NULL,
    contractor_id bigint NOT NULL,
    supervisor_id bigint NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    deadline date,
    section_leader_id bigint,
    total_points integer DEFAULT 0 NOT NULL,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.project_sections OWNER TO postgres;

--
-- Name: TABLE project_sections; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.project_sections IS '标段表';


--
-- Name: COLUMN project_sections.contractor_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.project_sections.contractor_id IS '负责乙方公司 ID';


--
-- Name: COLUMN project_sections.supervisor_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.project_sections.supervisor_id IS '负责监理公司 ID';


--
-- Name: COLUMN project_sections.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.project_sections.status IS '标段状态（0:筹备 1:进行中 2:已完成 3:已验收）';


--
-- Name: project_sections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.project_sections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.project_sections_id_seq OWNER TO postgres;

--
-- Name: project_sections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.project_sections_id_seq OWNED BY public.project_sections.id;


--
-- Name: projects; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.projects (
    id bigint NOT NULL,
    project_name character varying(100) NOT NULL,
    project_type character varying(50) NOT NULL,
    project_sub_type character varying(50) NOT NULL,
    construction_type character varying(50),
    description text,
    start_date date NOT NULL,
    end_date date NOT NULL,
    budget numeric(18,2),
    project_manager_id bigint NOT NULL,
    company_id bigint NOT NULL,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.projects OWNER TO postgres;

--
-- Name: TABLE projects; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.projects IS '项目表';


--
-- Name: COLUMN projects.project_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.projects.project_type IS '项目大类（施工类项目/技术服务类项目）';


--
-- Name: COLUMN projects.project_sub_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.projects.project_sub_type IS '项目小类（如视频监控施工项目）';


--
-- Name: COLUMN projects.construction_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.projects.construction_type IS '施工类型（新建/维修/运维）';


--
-- Name: COLUMN projects.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.projects.status IS '项目状态（0:筹备 1:进行中 2:已完成 3:已验收 4:运维中）';


--
-- Name: projects_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.projects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.projects_id_seq OWNER TO postgres;

--
-- Name: projects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.projects_id_seq OWNED BY public.projects.id;


--
-- Name: risk_rectification_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.risk_rectification_items (
    id bigint NOT NULL,
    audit_id bigint NOT NULL,
    subtask_instance_id bigint NOT NULL,
    point_id bigint NOT NULL,
    risk_description character varying(500) NOT NULL,
    rectification_requirement character varying(1000) NOT NULL,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.risk_rectification_items OWNER TO postgres;

--
-- Name: TABLE risk_rectification_items; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.risk_rectification_items IS '风险整改项表';


--
-- Name: COLUMN risk_rectification_items.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.risk_rectification_items.status IS '状态（0:待整改 1:整改中 2:已关闭 3:整改不通过）';


--
-- Name: risk_rectification_items_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.risk_rectification_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.risk_rectification_items_id_seq OWNER TO postgres;

--
-- Name: risk_rectification_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.risk_rectification_items_id_seq OWNED BY public.risk_rectification_items.id;


--
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_permissions (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.role_permissions OWNER TO postgres;

--
-- Name: TABLE role_permissions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.role_permissions IS '角色权限表（多对多关系）';


--
-- Name: role_permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.role_permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.role_permissions_id_seq OWNER TO postgres;

--
-- Name: role_permissions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.role_permissions_id_seq OWNED BY public.role_permissions.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    role_name character varying(50) NOT NULL,
    role_code character varying(50) NOT NULL,
    role_description character varying(200),
    company_type_id bigint NOT NULL,
    is_system_protected boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- Name: TABLE roles; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.roles IS '角色表';


--
-- Name: COLUMN roles.company_type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.company_type_id IS '公司类型 ID（1:甲方 2:乙方 3:监理 4:系统）';


--
-- Name: COLUMN roles.is_system_protected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.roles.is_system_protected IS '是否系统保护（system_admin 角色不可删除）';


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_seq OWNER TO postgres;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: subtask_audit_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subtask_audit_records (
    id bigint NOT NULL,
    subtask_instance_id bigint NOT NULL,
    point_id bigint NOT NULL,
    auditor_id bigint NOT NULL,
    audit_type character varying(20) NOT NULL,
    audit_result character varying(20) NOT NULL,
    audit_opinion text,
    audited_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subtask_audit_records OWNER TO postgres;

--
-- Name: TABLE subtask_audit_records; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.subtask_audit_records IS '子任务审核记录表';


--
-- Name: COLUMN subtask_audit_records.audit_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_audit_records.audit_type IS '审核类型（team:施工队长 manager:项目管理人员 supervisor:监理 party_a:甲方）';


--
-- Name: COLUMN subtask_audit_records.audit_result; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_audit_records.audit_result IS '审核结果（pass:通过 risk_pass:带风险通过 reject:驳回）';


--
-- Name: subtask_audit_records_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subtask_audit_records_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subtask_audit_records_id_seq OWNER TO postgres;

--
-- Name: subtask_audit_records_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subtask_audit_records_id_seq OWNED BY public.subtask_audit_records.id;


--
-- Name: subtask_definitions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subtask_definitions (
    id bigint NOT NULL,
    subtask_name character varying(100) NOT NULL,
    subtask_code character varying(50) NOT NULL,
    display_order integer DEFAULT 10 NOT NULL,
    predecessor_subtask_id bigint,
    project_sub_type character varying(50) NOT NULL,
    construction_type character varying(50) NOT NULL,
    audit_standard text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subtask_definitions OWNER TO postgres;

--
-- Name: TABLE subtask_definitions; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.subtask_definitions IS '子任务定义表';


--
-- Name: COLUMN subtask_definitions.predecessor_subtask_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_definitions.predecessor_subtask_id IS '前序子任务 ID（工艺上的前置任务）';


--
-- Name: COLUMN subtask_definitions.project_sub_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_definitions.project_sub_type IS '适用项目小类';


--
-- Name: COLUMN subtask_definitions.construction_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_definitions.construction_type IS '适用施工类型（新建/维修/运维）';


--
-- Name: subtask_definitions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subtask_definitions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subtask_definitions_id_seq OWNER TO postgres;

--
-- Name: subtask_definitions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subtask_definitions_id_seq OWNED BY public.subtask_definitions.id;


--
-- Name: subtask_instances; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subtask_instances (
    id bigint NOT NULL,
    point_id bigint NOT NULL,
    subtask_id bigint NOT NULL,
    status character varying(50) DEFAULT '未开始'::character varying,
    started_at timestamp without time zone,
    completed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subtask_instances OWNER TO postgres;

--
-- Name: TABLE subtask_instances; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.subtask_instances IS '子任务实例表';


--
-- Name: COLUMN subtask_instances.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_instances.status IS '子任务状态（未开始/进行中/待审核/审核通过/审核不通过/带风险通过/已报废）';


--
-- Name: subtask_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subtask_instances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subtask_instances_id_seq OWNER TO postgres;

--
-- Name: subtask_instances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subtask_instances_id_seq OWNED BY public.subtask_instances.id;


--
-- Name: subtask_scrap_applications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subtask_scrap_applications (
    id bigint NOT NULL,
    subtask_instance_id bigint NOT NULL,
    point_id bigint NOT NULL,
    applicant_id bigint NOT NULL,
    scrap_reason text NOT NULL,
    evidence_json jsonb,
    status character varying(50) DEFAULT 'pending'::character varying,
    contractor_audit_id bigint,
    supervisor_audit_id bigint,
    party_a_audit_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subtask_scrap_applications OWNER TO postgres;

--
-- Name: TABLE subtask_scrap_applications; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.subtask_scrap_applications IS '子任务作废申请表';


--
-- Name: COLUMN subtask_scrap_applications.evidence_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_scrap_applications.evidence_json IS '证据材料（JSON 格式）';


--
-- Name: COLUMN subtask_scrap_applications.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_scrap_applications.status IS '状态（pending/contractor_approved/supervisor_approved/party_a_approved/rejected）';


--
-- Name: subtask_scrap_applications_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subtask_scrap_applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subtask_scrap_applications_id_seq OWNER TO postgres;

--
-- Name: subtask_scrap_applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subtask_scrap_applications_id_seq OWNED BY public.subtask_scrap_applications.id;


--
-- Name: subtask_scrap_audits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subtask_scrap_audits (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    auditor_id bigint NOT NULL,
    auditor_role character varying(50) NOT NULL,
    audit_result character varying(20) NOT NULL,
    audit_opinion text,
    audited_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.subtask_scrap_audits OWNER TO postgres;

--
-- Name: TABLE subtask_scrap_audits; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.subtask_scrap_audits IS '子任务作废审核表';


--
-- Name: COLUMN subtask_scrap_audits.auditor_role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.subtask_scrap_audits.auditor_role IS '审核人角色（contractor/supervisor/party_a）';


--
-- Name: subtask_scrap_audits_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.subtask_scrap_audits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.subtask_scrap_audits_id_seq OWNER TO postgres;

--
-- Name: subtask_scrap_audits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.subtask_scrap_audits_id_seq OWNED BY public.subtask_scrap_audits.id;


--
-- Name: supervision_weekly_reports; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.supervision_weekly_reports (
    id bigint NOT NULL,
    supervisor_id bigint NOT NULL,
    project_id bigint,
    report_week date NOT NULL,
    content jsonb,
    pdf_url character varying(500),
    status character varying(50) DEFAULT 'draft'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.supervision_weekly_reports OWNER TO postgres;

--
-- Name: TABLE supervision_weekly_reports; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.supervision_weekly_reports IS '监理周报表';


--
-- Name: COLUMN supervision_weekly_reports.report_week; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.supervision_weekly_reports.report_week IS '报告周次（周一日期）';


--
-- Name: COLUMN supervision_weekly_reports.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.supervision_weekly_reports.status IS '状态（draft:草稿 submitted:已提交 approved:已审核）';


--
-- Name: supervision_weekly_reports_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.supervision_weekly_reports_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.supervision_weekly_reports_id_seq OWNER TO postgres;

--
-- Name: supervision_weekly_reports_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.supervision_weekly_reports_id_seq OWNED BY public.supervision_weekly_reports.id;


--
-- Name: system_configs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.system_configs (
    id bigint NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text,
    config_type character varying(20),
    description text,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.system_configs OWNER TO postgres;

--
-- Name: TABLE system_configs; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.system_configs IS '系统配置表';


--
-- Name: COLUMN system_configs.config_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.system_configs.config_type IS '配置类型（string/int/boolean/json）';


--
-- Name: system_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.system_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.system_configs_id_seq OWNER TO postgres;

--
-- Name: system_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.system_configs_id_seq OWNED BY public.system_configs.id;


--
-- Name: table_structure_registry; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.table_structure_registry (
    id bigint NOT NULL,
    component_type_id bigint,
    subtask_id bigint,
    template_id bigint,
    table_name character varying(100) NOT NULL,
    table_type character varying(20) NOT NULL,
    table_comment character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT table_structure_registry_check CHECK ((((component_type_id IS NOT NULL) AND (subtask_id IS NULL)) OR ((component_type_id IS NULL) AND (subtask_id IS NOT NULL)) OR (template_id IS NOT NULL)))
);


ALTER TABLE public.table_structure_registry OWNER TO postgres;

--
-- Name: TABLE table_structure_registry; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.table_structure_registry IS '表结构注册表（管理所有动态表）';


--
-- Name: COLUMN table_structure_registry.table_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.table_structure_registry.table_type IS '表类型（attr_def:属性定义 attr_value:属性值 template_value:模板属性值）';


--
-- Name: table_structure_registry_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.table_structure_registry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.table_structure_registry_id_seq OWNER TO postgres;

--
-- Name: table_structure_registry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.table_structure_registry_id_seq OWNED BY public.table_structure_registry.id;


--
-- Name: task_assignment_users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.task_assignment_users (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    user_id bigint NOT NULL,
    assigned_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.task_assignment_users OWNER TO postgres;

--
-- Name: TABLE task_assignment_users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.task_assignment_users IS '任务分配用户关系表（多对多）';


--
-- Name: task_assignment_users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.task_assignment_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_assignment_users_id_seq OWNER TO postgres;

--
-- Name: task_assignment_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.task_assignment_users_id_seq OWNED BY public.task_assignment_users.id;


--
-- Name: task_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.task_assignments (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    section_id bigint NOT NULL,
    team_id bigint,
    user_id bigint,
    point_range character varying(100) NOT NULL,
    subtask_range character varying(200) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.task_assignments OWNER TO postgres;

--
-- Name: TABLE task_assignments; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.task_assignments IS '任务分配表';


--
-- Name: COLUMN task_assignments.point_range; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.task_assignments.point_range IS '点位范围（如"1-50"）';


--
-- Name: COLUMN task_assignments.subtask_range; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.task_assignments.subtask_range IS '子任务范围（如"选点，征地"）';


--
-- Name: COLUMN task_assignments.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.task_assignments.status IS '任务状态（0:未开始 1:进行中 2:已完成 3:已逾期）';


--
-- Name: task_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.task_assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.task_assignments_id_seq OWNER TO postgres;

--
-- Name: task_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.task_assignments_id_seq OWNED BY public.task_assignments.id;


--
-- Name: team_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.team_members (
    id bigint NOT NULL,
    team_id bigint NOT NULL,
    user_id bigint NOT NULL,
    is_leader boolean DEFAULT false,
    join_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.team_members OWNER TO postgres;

--
-- Name: TABLE team_members; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.team_members IS '施工队成员表';


--
-- Name: COLUMN team_members.is_leader; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.team_members.is_leader IS '是否队长';


--
-- Name: team_members_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.team_members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.team_members_id_seq OWNER TO postgres;

--
-- Name: team_members_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.team_members_id_seq OWNED BY public.team_members.id;


--
-- Name: user_online_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_online_logs (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    login_time timestamp without time zone NOT NULL,
    logout_time timestamp without time zone,
    duration_seconds integer,
    ip_address character varying(50),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_online_logs OWNER TO postgres;

--
-- Name: TABLE user_online_logs; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_online_logs IS '用户在线日志表';


--
-- Name: user_online_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_online_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_online_logs_id_seq OWNER TO postgres;

--
-- Name: user_online_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_online_logs_id_seq OWNED BY public.user_online_logs.id;


--
-- Name: user_role_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_role_requests (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    status integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_role_requests OWNER TO postgres;

--
-- Name: user_role_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_role_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_role_requests_id_seq OWNER TO postgres;

--
-- Name: user_role_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_role_requests_id_seq OWNED BY public.user_role_requests.id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- Name: TABLE user_roles; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_roles IS '用户角色表（多对多关系）';


--
-- Name: user_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_roles_id_seq OWNER TO postgres;

--
-- Name: user_roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_roles_id_seq OWNED BY public.user_roles.id;


--
-- Name: user_work_areas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_work_areas (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    work_area_id bigint NOT NULL,
    is_primary boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.user_work_areas OWNER TO postgres;

--
-- Name: TABLE user_work_areas; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.user_work_areas IS '用户作业区关系表（多对多关系）';


--
-- Name: COLUMN user_work_areas.is_primary; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.user_work_areas.is_primary IS '是否主要作业区（一个用户只能有一个主要作业区）';


--
-- Name: user_work_areas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_work_areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_work_areas_id_seq OWNER TO postgres;

--
-- Name: user_work_areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_work_areas_id_seq OWNED BY public.user_work_areas.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    email character varying(100),
    phone character varying(20),
    real_name character varying(50) NOT NULL,
    company_id bigint NOT NULL,
    approval_status integer DEFAULT 0,
    approved_by bigint,
    approved_at timestamp without time zone,
    rejection_reason text,
    status integer DEFAULT 0,
    is_system_protected boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    gender integer DEFAULT 0
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: TABLE users; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.users IS '用户表';


--
-- Name: COLUMN users.approval_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.approval_status IS '审批状态（0:待审批 1:通过 2:拒绝）';


--
-- Name: COLUMN users.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.status IS '用户状态（0:待审批 1:正常 2:禁用）';


--
-- Name: COLUMN users.is_system_protected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.is_system_protected IS '是否系统保护（admin 用户不可删除）';


--
-- Name: COLUMN users.gender; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.users.gender IS '性别（0:未知 1:男 2:女）';


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: v_point_subtasks; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_point_subtasks AS
 SELECT p.id AS point_id,
    p.point_code,
    p.section_id,
    p.work_area_id,
    p.status AS point_status,
    sd.id AS subtask_id,
    sd.subtask_name,
    sd.subtask_code,
    si.status AS subtask_status,
    si.started_at,
    si.completed_at
   FROM ((public.points p
     JOIN public.subtask_instances si ON ((p.id = si.point_id)))
     JOIN public.subtask_definitions sd ON ((si.subtask_id = sd.id)));


ALTER VIEW public.v_point_subtasks OWNER TO postgres;

--
-- Name: v_project_sections; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_project_sections AS
 SELECT p.id AS project_id,
    p.project_name,
    p.project_type,
    p.project_sub_type,
    p.construction_type,
    p.status AS project_status,
    ps.id AS section_id,
    ps.section_name,
    ps.status AS section_status,
    ps.total_points,
    contractor.company_name AS contractor_name,
    supervisor.company_name AS supervisor_name
   FROM (((public.projects p
     JOIN public.project_sections ps ON ((p.id = ps.project_id)))
     JOIN public.companies contractor ON ((ps.contractor_id = contractor.id)))
     JOIN public.companies supervisor ON ((ps.supervisor_id = supervisor.id)));


ALTER VIEW public.v_project_sections OWNER TO postgres;

--
-- Name: v_user_roles; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.v_user_roles AS
 SELECT u.id AS user_id,
    u.username,
    u.real_name,
    u.email,
    u.phone,
    c.company_name,
    ct.type_name AS company_type,
    r.role_name,
    r.role_code,
    u.status AS user_status,
    u.created_at
   FROM ((((public.users u
     JOIN public.companies c ON ((u.company_id = c.id)))
     JOIN public.company_types ct ON ((c.type_id = ct.id)))
     JOIN public.user_roles ur ON ((u.id = ur.user_id)))
     JOIN public.roles r ON ((ur.role_id = r.id)));


ALTER VIEW public.v_user_roles OWNER TO postgres;

--
-- Name: work_areas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.work_areas (
    id bigint NOT NULL,
    work_area_name character varying(100) NOT NULL,
    work_area_code character varying(50),
    company_id bigint NOT NULL,
    leader_name character varying(50),
    leader_phone character varying(20),
    geographic_range text,
    max_capacity integer DEFAULT 1000,
    description text,
    is_system_protected boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.work_areas OWNER TO postgres;

--
-- Name: TABLE work_areas; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.work_areas IS '作业区表';


--
-- Name: COLUMN work_areas.company_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.work_areas.company_id IS '所属公司 ID';


--
-- Name: COLUMN work_areas.max_capacity; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.work_areas.max_capacity IS '最大点位容量';


--
-- Name: COLUMN work_areas.is_system_protected; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.work_areas.is_system_protected IS '是否系统保护（true 时不可删除）';


--
-- Name: work_areas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.work_areas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.work_areas_id_seq OWNER TO postgres;

--
-- Name: work_areas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.work_areas_id_seq OWNED BY public.work_areas.id;


--
-- Name: attribute_templates id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_templates ALTER COLUMN id SET DEFAULT nextval('public.attribute_templates_id_seq'::regclass);


--
-- Name: backup_records id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backup_records ALTER COLUMN id SET DEFAULT nextval('public.backup_records_id_seq'::regclass);


--
-- Name: companies id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies ALTER COLUMN id SET DEFAULT nextval('public.companies_id_seq'::regclass);


--
-- Name: company_types id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_types ALTER COLUMN id SET DEFAULT nextval('public.company_types_id_seq'::regclass);


--
-- Name: component_instances id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_instances ALTER COLUMN id SET DEFAULT nextval('public.component_instances_id_seq'::regclass);


--
-- Name: component_replacement_applications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_applications ALTER COLUMN id SET DEFAULT nextval('public.component_replacement_applications_id_seq'::regclass);


--
-- Name: component_replacement_history id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history ALTER COLUMN id SET DEFAULT nextval('public.component_replacement_history_id_seq'::regclass);


--
-- Name: component_types id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_types ALTER COLUMN id SET DEFAULT nextval('public.component_types_id_seq'::regclass);


--
-- Name: construction_orgs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs ALTER COLUMN id SET DEFAULT nextval('public.construction_orgs_id_seq'::regclass);


--
-- Name: construction_teams id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_teams ALTER COLUMN id SET DEFAULT nextval('public.construction_teams_id_seq'::regclass);


--
-- Name: device_model_components id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_model_components ALTER COLUMN id SET DEFAULT nextval('public.device_model_components_id_seq'::regclass);


--
-- Name: device_models id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_models ALTER COLUMN id SET DEFAULT nextval('public.device_models_id_seq'::regclass);


--
-- Name: dynamic_attr_def id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_def ALTER COLUMN id SET DEFAULT nextval('public.dynamic_attr_def_id_seq'::regclass);


--
-- Name: dynamic_attr_value id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_value ALTER COLUMN id SET DEFAULT nextval('public.dynamic_attr_value_id_seq'::regclass);


--
-- Name: final_settlements id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.final_settlements ALTER COLUMN id SET DEFAULT nextval('public.final_settlements_id_seq'::regclass);


--
-- Name: inspection_records id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inspection_records ALTER COLUMN id SET DEFAULT nextval('public.inspection_records_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: operation_logs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_logs ALTER COLUMN id SET DEFAULT nextval('public.operation_logs_id_seq'::regclass);


--
-- Name: page_access_logs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.page_access_logs ALTER COLUMN id SET DEFAULT nextval('public.page_access_logs_id_seq'::regclass);


--
-- Name: permissions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions ALTER COLUMN id SET DEFAULT nextval('public.permissions_id_seq'::regclass);


--
-- Name: points id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.points ALTER COLUMN id SET DEFAULT nextval('public.points_id_seq'::regclass);


--
-- Name: progress_payment_applications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_applications ALTER COLUMN id SET DEFAULT nextval('public.progress_payment_applications_id_seq'::regclass);


--
-- Name: progress_payment_audits id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_audits ALTER COLUMN id SET DEFAULT nextval('public.progress_payment_audits_id_seq'::regclass);


--
-- Name: project_authorizations id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_authorizations ALTER COLUMN id SET DEFAULT nextval('public.project_authorizations_id_seq'::regclass);


--
-- Name: project_quantities id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities ALTER COLUMN id SET DEFAULT nextval('public.project_quantities_id_seq'::regclass);


--
-- Name: project_sections id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections ALTER COLUMN id SET DEFAULT nextval('public.project_sections_id_seq'::regclass);


--
-- Name: projects id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects ALTER COLUMN id SET DEFAULT nextval('public.projects_id_seq'::regclass);


--
-- Name: risk_rectification_items id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.risk_rectification_items ALTER COLUMN id SET DEFAULT nextval('public.risk_rectification_items_id_seq'::regclass);


--
-- Name: role_permissions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions ALTER COLUMN id SET DEFAULT nextval('public.role_permissions_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Name: subtask_audit_records id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_audit_records ALTER COLUMN id SET DEFAULT nextval('public.subtask_audit_records_id_seq'::regclass);


--
-- Name: subtask_definitions id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_definitions ALTER COLUMN id SET DEFAULT nextval('public.subtask_definitions_id_seq'::regclass);


--
-- Name: subtask_instances id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_instances ALTER COLUMN id SET DEFAULT nextval('public.subtask_instances_id_seq'::regclass);


--
-- Name: subtask_scrap_applications id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_applications ALTER COLUMN id SET DEFAULT nextval('public.subtask_scrap_applications_id_seq'::regclass);


--
-- Name: subtask_scrap_audits id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_audits ALTER COLUMN id SET DEFAULT nextval('public.subtask_scrap_audits_id_seq'::regclass);


--
-- Name: supervision_weekly_reports id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supervision_weekly_reports ALTER COLUMN id SET DEFAULT nextval('public.supervision_weekly_reports_id_seq'::regclass);


--
-- Name: system_configs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_configs ALTER COLUMN id SET DEFAULT nextval('public.system_configs_id_seq'::regclass);


--
-- Name: table_structure_registry id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_structure_registry ALTER COLUMN id SET DEFAULT nextval('public.table_structure_registry_id_seq'::regclass);


--
-- Name: task_assignment_users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignment_users ALTER COLUMN id SET DEFAULT nextval('public.task_assignment_users_id_seq'::regclass);


--
-- Name: task_assignments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments ALTER COLUMN id SET DEFAULT nextval('public.task_assignments_id_seq'::regclass);


--
-- Name: team_members id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members ALTER COLUMN id SET DEFAULT nextval('public.team_members_id_seq'::regclass);


--
-- Name: user_online_logs id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_online_logs ALTER COLUMN id SET DEFAULT nextval('public.user_online_logs_id_seq'::regclass);


--
-- Name: user_role_requests id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_requests ALTER COLUMN id SET DEFAULT nextval('public.user_role_requests_id_seq'::regclass);


--
-- Name: user_roles id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles ALTER COLUMN id SET DEFAULT nextval('public.user_roles_id_seq'::regclass);


--
-- Name: user_work_areas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_work_areas ALTER COLUMN id SET DEFAULT nextval('public.user_work_areas_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: work_areas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.work_areas ALTER COLUMN id SET DEFAULT nextval('public.work_areas_id_seq'::regclass);


--
-- Data for Name: attribute_templates; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.attribute_templates (id, template_name, component_type_id, table_id, is_default, description, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: backup_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.backup_records (id, backup_type, backup_path, backup_size, status, started_at, completed_at, created_at) FROM stdin;
\.


--
-- Data for Name: companies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.companies (id, company_name, type_id, unified_social_credit_code, contact_person, contact_phone, contact_email, address, status, description, is_system_protected, created_by, updated_by, created_at, updated_at, allow_anonymous_register) FROM stdin;
5	国家管网集团云南分公司	1						active		f	\N	\N	2026-03-10 20:13:58.078407	2026-03-10 20:13:58.078466	f
7	福建省邮电规划设计院有限公司	2						active		f	\N	\N	2026-03-10 22:53:00.766193	2026-03-10 22:53:00.766253	f
8	陕西威远建设工程有限公司	2						active		f	\N	\N	2026-03-10 22:53:47.739029	2026-03-10 22:53:47.739069	f
9	湖北瑞通胜达电子科技有限公司	2						active		f	\N	\N	2026-03-10 23:00:06.182039	2026-03-10 23:00:06.182088	f
10	中移建设有限公司	2						active		f	\N	\N	2026-03-10 23:00:36.430123	2026-03-10 23:00:36.430178	f
11	测试乙方公司	2	123	严骏	1231232141	111@qq.com	2	active	3	f	\N	\N	2026-03-12 20:15:08.079011	2026-03-12 20:15:08.079095	f
1	北京其点技术服务有限公司	4	\N	\N	\N	\N	\N	active	系统开发方	t	\N	\N	2026-03-10 00:02:52.02561	2026-03-10 00:02:52.02561	f
4	国家管网集团华中分公司	1						active		f	\N	\N	2026-03-10 20:13:32.970757	2026-03-10 20:13:32.972671	t
3	测试甲方公司	1						active	甲方公司	f	\N	\N	2026-03-10 18:41:31.771107	2026-03-10 18:41:31.771107	t
6	云南仁邦信息工程有限公司	2						active		f	\N	\N	2026-03-10 22:52:13.876913	2026-03-10 22:52:13.876974	t
12	胜利油田中睿建设监理有限责任公司	3						active		f	\N	\N	2026-03-13 12:00:43.358118	2026-03-13 12:00:43.358175	t
\.


--
-- Data for Name: company_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.company_types (id, type_name, description, is_system_protected, created_at, updated_at) FROM stdin;
1	甲方公司	项目建设方	f	2026-03-10 00:02:52.000951	2026-03-10 00:02:52.000951
2	乙方公司	项目施工方	f	2026-03-10 00:02:52.000951	2026-03-10 00:02:52.000951
3	监理公司	项目监理方	f	2026-03-10 00:02:52.000951	2026-03-10 00:02:52.000951
4	软件所有者公司	系统开发方（北京其点技术服务有限公司）	t	2026-03-10 00:02:52.000951	2026-03-10 00:02:52.000951
\.


--
-- Data for Name: component_instances; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component_instances (id, component_type_id, point_id, instance_name, instance_order, template_id, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: component_replacement_applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component_replacement_applications (id, component_instance_id, applicant_id, replacement_reason, damage_description, expected_date, suggested_template_id, status, team_leader_audit_id, manager_audit_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: component_replacement_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component_replacement_history (id, point_id, component_type_id, old_component_id, new_component_id, replacement_reason, replacement_date, operator_id, created_at) FROM stdin;
\.


--
-- Data for Name: component_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.component_types (id, component_name, code, description, display_order, created_at, updated_at) FROM stdin;
1	监控杆	MONITORING_POLE	用于安装摄像头的支撑结构	10	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
2	摄像头	CAMERA	监控摄像头	20	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
3	补光灯	FILL_LIGHT	补光设备	30	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
4	流量卡	DATA_CARD	4G/5G 流量卡	40	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
5	AI 盒子	AI_BOX	前端计算设备	50	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
6	光伏板	SOLAR_PANEL	太阳能光伏板	60	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
7	蓄电池	BATTERY	储能蓄电池	70	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
8	太阳能控制器	SOLAR_CONTROLLER	太阳能充电控制器	80	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
9	网关	GATEWAY	网络网关	90	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
10	路由器	ROUTER	网络路由器	100	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
11	风向标	WIND_VANE	风向监测设备	110	2026-03-10 00:02:52.31419	2026-03-10 00:02:52.31419
\.


--
-- Data for Name: construction_orgs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.construction_orgs (id, section_id, project_manager_id, technical_manager_id, safety_manager_id, quality_manager_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: construction_teams; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.construction_teams (id, team_name, org_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: device_model_components; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.device_model_components (id, device_model_id, component_id, quantity, display_order, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: device_models; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.device_models (id, model_name, description, applicable_project_type, applicable_project_sub_type, applicable_construction_type, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: dynamic_attr_def; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dynamic_attr_def (id, table_id, component_type_id, subtask_id, column_name, column_type, max_length, attribute_name, attribute_order, unit, audit_standard, is_required, default_value, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: dynamic_attr_value; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dynamic_attr_value (id, component_instance_id, subtask_instance_id, point_id, template_id, table_name, record_id, attribute_data, is_from_template, status, creator_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: final_settlements; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.final_settlements (id, project_id, settlement_number, contract_amount, total_completed_quantity, total_approved_amount, adjustment_amount, final_amount, paid_amount, balance_amount, settlement_report_url, audit_report_url, status, approved_by, approved_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: inspection_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.inspection_records (id, point_id, inspector_id, inspection_type, inspection_result, inspection_comment, reviewer_id, review_opinion, created_at) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, user_id, notification_type, title, content, is_read, created_at) FROM stdin;
\.


--
-- Data for Name: operation_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.operation_logs (id, user_id, operation_type, operation_content, operation_result, ip_address, created_at) FROM stdin;
\.


--
-- Data for Name: page_access_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.page_access_logs (id, user_id, page_url, page_name, accessed_at) FROM stdin;
\.


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissions (id, permission_name, permission_code, permission_description, created_at) FROM stdin;
1	项目管理	project_manage	项目管理相关权限	2026-03-10 00:02:52.147002
2	项目创建	project_create	创建项目	2026-03-10 00:02:52.147002
3	项目查看	project_view	查看项目	2026-03-10 00:02:52.147002
4	项目编辑	project_edit	编辑项目	2026-03-10 00:02:52.147002
5	项目删除	project_delete	删除项目	2026-03-10 00:02:52.147002
6	标段管理	section_manage	标段管理相关权限	2026-03-10 00:02:52.147002
7	标段创建	section_create	创建标段	2026-03-10 00:02:52.147002
8	标段查看	section_view	查看标段	2026-03-10 00:02:52.147002
9	标段编辑	section_edit	编辑标段	2026-03-10 00:02:52.147002
10	标段删除	section_delete	删除标段	2026-03-10 00:02:52.147002
11	点位管理	point_manage	点位管理相关权限	2026-03-10 00:02:52.147002
12	点位创建	point_create	创建点位	2026-03-10 00:02:52.147002
13	点位查看	point_view	查看点位	2026-03-10 00:02:52.147002
14	点位编辑	point_edit	编辑点位	2026-03-10 00:02:52.147002
15	点位删除	point_delete	删除点位	2026-03-10 00:02:52.147002
16	设备模型管理	device_model_manage	设备模型管理相关权限	2026-03-10 00:02:52.147002
17	施工管理	construction_manage	施工管理相关权限	2026-03-10 00:02:52.147002
18	任务分配	task_assign	分配施工任务	2026-03-10 00:02:52.147002
19	进度查看	progress_view	查看施工进度	2026-03-10 00:02:52.147002
20	审核管理	audit_manage	审核管理相关权限	2026-03-10 00:02:52.147002
21	子任务审核	subtask_audit	审核子任务	2026-03-10 00:02:52.147002
22	点位审核	point_audit	审核点位	2026-03-10 00:02:52.147002
23	结算管理	settlement_manage	结算管理相关权限	2026-03-10 00:02:52.147002
24	工程量统计	quantity_statistics	统计工程量	2026-03-10 00:02:52.147002
25	进度款审核	payment_audit	审核进度款	2026-03-10 00:02:52.147002
26	系统管理	system_manage	系统管理相关权限	2026-03-10 00:02:52.147002
27	用户管理	user_manage	管理用户	2026-03-10 00:02:52.147002
28	角色管理	role_manage	管理角色	2026-03-10 00:02:52.147002
29	权限管理	permission_manage	管理权限	2026-03-10 00:02:52.147002
\.


--
-- Data for Name: point_status; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.point_status (point_id, status, subtask_stats, last_updated) FROM stdin;
\.


--
-- Data for Name: points; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.points (id, point_name, point_code, section_id, work_area_id, device_model_id, latitude, longitude, geom, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: progress_payment_applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.progress_payment_applications (id, project_id, section_id, application_number, application_period_start, application_period_end, total_quantity, unit_price, total_amount, attachment_urls, status, contractor_audit_id, supervisor_audit_id, party_a_audit_id, paid_amount, paid_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: progress_payment_audits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.progress_payment_audits (id, application_id, auditor_id, auditor_role, audit_result, audit_opinion, audited_at) FROM stdin;
\.


--
-- Data for Name: project_authorizations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_authorizations (id, project_id, authorized_user_id, granted_by_user_id, granted_at, expires_at, is_active) FROM stdin;
\.


--
-- Data for Name: project_quantities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_quantities (id, project_id, section_id, point_id, subtask_id, subtask_instance_id, quantity, unit, status, confirmed_by, confirmed_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: project_sections; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.project_sections (id, section_name, description, project_id, contractor_id, supervisor_id, start_date, end_date, deadline, section_leader_id, total_points, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: projects; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.projects (id, project_name, project_type, project_sub_type, construction_type, description, start_date, end_date, budget, project_manager_id, company_id, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: risk_rectification_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.risk_rectification_items (id, audit_id, subtask_instance_id, point_id, risk_description, rectification_requirement, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permissions (id, role_id, permission_id, created_at) FROM stdin;
1	1	1	2026-03-10 00:02:52.173937
2	1	2	2026-03-10 00:02:52.173937
3	1	3	2026-03-10 00:02:52.173937
4	1	4	2026-03-10 00:02:52.173937
5	1	5	2026-03-10 00:02:52.173937
6	1	6	2026-03-10 00:02:52.173937
7	1	7	2026-03-10 00:02:52.173937
8	1	8	2026-03-10 00:02:52.173937
9	1	9	2026-03-10 00:02:52.173937
10	1	10	2026-03-10 00:02:52.173937
11	1	11	2026-03-10 00:02:52.173937
12	1	12	2026-03-10 00:02:52.173937
13	1	13	2026-03-10 00:02:52.173937
14	1	14	2026-03-10 00:02:52.173937
15	1	15	2026-03-10 00:02:52.173937
16	1	16	2026-03-10 00:02:52.173937
17	1	17	2026-03-10 00:02:52.173937
18	1	18	2026-03-10 00:02:52.173937
19	1	19	2026-03-10 00:02:52.173937
20	1	20	2026-03-10 00:02:52.173937
21	1	21	2026-03-10 00:02:52.173937
22	1	22	2026-03-10 00:02:52.173937
23	1	23	2026-03-10 00:02:52.173937
24	1	24	2026-03-10 00:02:52.173937
25	1	25	2026-03-10 00:02:52.173937
26	1	26	2026-03-10 00:02:52.173937
27	1	27	2026-03-10 00:02:52.173937
28	1	28	2026-03-10 00:02:52.173937
29	1	29	2026-03-10 00:02:52.173937
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, role_name, role_code, role_description, company_type_id, is_system_protected, created_at) FROM stdin;
1	系统管理员	system_admin	系统最高权限管理员	4	t	2026-03-10 00:02:52.128075
2	甲方管理员	jiafang_admin	甲方公司的最高权限管理员	1	f	2026-03-10 00:02:52.128075
3	甲方项目经理	jiafang_project_manager	甲方项目经理	1	f	2026-03-10 00:02:52.128075
4	甲方项目经办人	jiafang_project_operator	甲方项目经办人	1	f	2026-03-10 00:02:52.128075
5	甲方作业区项目主管	jiafang_work_area_supervisor	作业区的负责人，管理作业区内的项目实施	1	f	2026-03-10 00:02:52.128075
6	甲方作业区项目经理	jiafang_work_area_manager	作业区的项目执行人员，负责作业区内项目的具体推进	1	f	2026-03-10 00:02:52.128075
7	甲方作业区项目经办人	jiafang_work_area_operator	作业区的项目协调人员，协助管理项目事务	1	f	2026-03-10 00:02:52.128075
8	乙方管理员	yifang_admin	乙方公司的最高权限管理员	2	f	2026-03-10 00:02:52.128075
9	乙方项目经理	yifang_project_manager	乙方项目经理	2	f	2026-03-10 00:02:52.128075
10	乙方技术负责人	yifang_technical_manager	乙方技术负责人	2	f	2026-03-10 00:02:52.128075
11	乙方安全负责人	yifang_safety_manager	乙方安全负责人	2	f	2026-03-10 00:02:52.128075
12	乙方质量负责人	yifang_quality_manager	乙方质量负责人	2	f	2026-03-10 00:02:52.128075
13	乙方施工队长	yifang_construction_leader	乙方施工队长	2	f	2026-03-10 00:02:52.128075
14	乙方施工人员	yifang_construction_worker	乙方施工人员	2	f	2026-03-10 00:02:52.128075
15	乙方材料管理员	yifang_material_admin	乙方材料管理员	2	f	2026-03-10 00:02:52.128075
16	监理方管理员	jianli_admin	监理公司的最高权限管理员	3	f	2026-03-10 00:02:52.128075
17	监理方项目经理	jianli_project_manager	监理方项目经理	3	f	2026-03-10 00:02:52.128075
18	监理方项目经办人	jianli_project_operator	监理方项目经办人	3	f	2026-03-10 00:02:52.128075
20	乙方资料员	yifang_document	乙方资料整理员	2	f	2026-03-11 07:14:43.973198
21	test	test11		1	f	2026-03-12 12:29:08.800115
\.


--
-- Data for Name: spatial_ref_sys; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.spatial_ref_sys (srid, auth_name, auth_srid, srtext, proj4text) FROM stdin;
\.


--
-- Data for Name: subtask_audit_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subtask_audit_records (id, subtask_instance_id, point_id, auditor_id, audit_type, audit_result, audit_opinion, audited_at) FROM stdin;
\.


--
-- Data for Name: subtask_definitions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subtask_definitions (id, subtask_name, subtask_code, display_order, predecessor_subtask_id, project_sub_type, construction_type, audit_standard, created_at, updated_at) FROM stdin;
1	选点	SITE_SELECTION	10	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
2	征地	LAND_REQUISITION	20	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
3	基坑	FOUNDATION_PIT	30	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
4	浇筑	CONCRETE_POURING	40	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
5	设备组装	EQUIPMENT_ASSEMBLY	50	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
6	立杆	POLE_INSTALLATION	60	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
7	设备调测	EQUIPMENT_DEBUGGING	70	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
8	接入平台	PLATFORM_ACCESS	80	\N	视频监控施工项目	新建	\N	2026-03-10 00:02:52.427059	2026-03-10 00:02:52.427059
\.


--
-- Data for Name: subtask_instances; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subtask_instances (id, point_id, subtask_id, status, started_at, completed_at, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: subtask_scrap_applications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subtask_scrap_applications (id, subtask_instance_id, point_id, applicant_id, scrap_reason, evidence_json, status, contractor_audit_id, supervisor_audit_id, party_a_audit_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: subtask_scrap_audits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subtask_scrap_audits (id, application_id, auditor_id, auditor_role, audit_result, audit_opinion, audited_at) FROM stdin;
\.


--
-- Data for Name: supervision_weekly_reports; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.supervision_weekly_reports (id, supervisor_id, project_id, report_week, content, pdf_url, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: system_configs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.system_configs (id, config_key, config_value, config_type, description, updated_at) FROM stdin;
1	batch_operation_max_points	50	int	批量操作最大点位数量（缺省值）	2026-03-10 00:02:52.665435
2	batch_operation_max_points_limit	200	int	批量操作最大点位数量（上限）	2026-03-10 00:02:52.665435
3	login_captcha_enabled	true	boolean	是否启用登录验证码	2026-03-10 00:02:52.665435
4	sms_captcha_enabled	true	boolean	是否启用短信验证码	2026-03-10 00:02:52.665435
10	sms-length	6	int	短信验证码长度（默认 6 位）	2026-03-10 12:36:17.728607
6	captcha-type	none	string	验证码类型：none-不用验证码，image-图形验证码，sms-手机验证码	2026-03-12 20:12:52.982
7	captcha-length	4	int	图形验证码长度	2026-03-12 20:12:53.02
8	captcha-expire-minutes	5	int	验证码过期时间（分钟）	2026-03-12 20:12:53.058
9	sms-interval-seconds	60	int	短信验证码发送间隔（秒）	2026-03-12 20:12:53.094
\.


--
-- Data for Name: table_structure_registry; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.table_structure_registry (id, component_type_id, subtask_id, template_id, table_name, table_type, table_comment, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: task_assignment_users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.task_assignment_users (id, task_id, user_id, assigned_at) FROM stdin;
\.


--
-- Data for Name: task_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.task_assignments (id, project_id, section_id, team_id, user_id, point_range, subtask_range, start_date, end_date, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: team_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.team_members (id, team_id, user_id, is_leader, join_time) FROM stdin;
\.


--
-- Data for Name: user_online_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_online_logs (id, user_id, login_time, logout_time, duration_seconds, ip_address, created_at) FROM stdin;
\.


--
-- Data for Name: user_role_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_role_requests (id, user_id, role_id, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (id, user_id, role_id, created_at) FROM stdin;
1	1	1	2026-03-10 00:02:52.160637
3	4	8	2026-03-11 08:38:18.902148
4	5	14	2026-03-11 08:38:30.88276
5	9	8	2026-03-11 22:45:22.874821
6	10	14	2026-03-11 22:45:23.152841
7	12	14	2026-03-11 22:45:23.671813
8	13	14	2026-03-11 22:45:23.671813
9	14	15	2026-03-12 07:06:57.948669
10	14	14	2026-03-12 07:06:57.948669
13	23	3	2026-03-12 21:41:12.004011
14	24	2	2026-03-12 21:48:43.90039
15	25	4	2026-03-12 21:48:43.90039
16	27	2	2026-03-12 22:49:59.300087
17	28	4	2026-03-12 22:49:59.300087
18	29	3	2026-03-12 22:49:59.300087
19	30	2	2026-03-12 23:14:00.174192
20	31	4	2026-03-12 23:14:00.174192
21	32	3	2026-03-12 23:14:00.174192
22	33	2	2026-03-12 23:17:56.313407
23	34	4	2026-03-12 23:17:56.313407
24	35	3	2026-03-12 23:17:56.313407
25	37	8	2026-03-12 23:41:25.693264
26	38	9	2026-03-12 23:41:25.693264
27	39	8	2026-03-13 00:36:37.340099
28	40	9	2026-03-13 00:36:37.340099
29	41	8	2026-03-13 00:51:17.695327
30	42	8	2026-03-13 11:40:01.137488
31	43	12	2026-03-13 11:40:01.137488
32	44	20	2026-03-13 11:40:01.137488
33	45	20	2026-03-13 11:43:03.892444
34	46	9	2026-03-13 11:44:16.181426
35	47	13	2026-03-13 11:44:16.181426
36	48	2	2026-03-13 12:04:49.793483
37	49	4	2026-03-13 12:04:49.793483
38	50	3	2026-03-13 12:04:49.793483
39	51	5	2026-03-13 12:04:49.793483
40	52	7	2026-03-13 12:04:49.793483
41	53	6	2026-03-13 12:04:49.793483
42	54	2	2026-03-13 12:04:49.793483
43	55	4	2026-03-13 12:04:49.793483
44	56	3	2026-03-13 12:04:49.793483
45	57	5	2026-03-13 12:04:49.793483
46	58	7	2026-03-13 12:04:49.793483
47	59	6	2026-03-13 12:04:49.793483
48	60	5	2026-03-13 12:04:49.793483
49	61	7	2026-03-13 12:04:49.793483
50	62	6	2026-03-13 12:04:49.793483
51	63	16	2026-03-13 12:04:49.793483
52	64	18	2026-03-13 12:04:49.793483
53	65	17	2026-03-13 12:04:49.793483
54	66	5	2026-03-13 12:06:19.724349
55	67	7	2026-03-13 12:06:19.724349
56	68	6	2026-03-13 12:06:19.724349
59	8	10	2026-03-13 20:34:43.551913
60	69	2	2026-03-14 01:37:21.857631
65	70	3	2026-03-14 10:49:30.54027
69	22	3	2026-03-14 11:47:27.868319
70	21	8	2026-03-14 12:14:56.559714
73	71	17	2026-03-14 12:31:22.984973
74	72	3	2026-03-14 12:38:50.036563
76	73	18	2026-03-14 13:09:54.532045
77	74	3	2026-03-14 13:32:03.76049
78	75	17	2026-03-14 14:08:47.896446
\.


--
-- Data for Name: user_work_areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_work_areas (id, user_id, work_area_id, is_primary, created_at, updated_at) FROM stdin;
2	51	18	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
3	52	18	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
4	53	18	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
5	57	13	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
6	58	13	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
7	59	13	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
8	60	12	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
9	61	12	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
10	62	12	f	2026-03-13 12:04:49.793483	2026-03-13 12:04:49.793483
11	66	17	f	2026-03-13 12:06:19.724349	2026-03-13 12:06:19.724349
12	67	17	f	2026-03-13 12:06:19.724349	2026-03-13 12:06:19.724349
13	68	17	f	2026-03-13 12:06:19.724349	2026-03-13 12:06:19.724349
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, password_hash, email, phone, real_name, company_id, approval_status, approved_by, approved_at, rejection_reason, status, is_system_protected, created_at, updated_at, gender) FROM stdin;
24	甲方测试11	$2a$10$agYioq0cIkDzFIdeRkFCeeTupN6fLHso7t9hAzGLZSCI0SW9xtqIS	jiafang11@test.com	13800138111	贾芳1	3	1	1	2026-03-12 21:48:43.986199	\N	1	f	2026-03-12 21:48:43.987305	2026-03-12 21:48:43.987372	1
25	甲方测试12	$2a$10$Ede35/N1bzyh2Stcx8VwPu.yLFh90i.atpS/7ocFJxh15oWq0QuGy	jiafang12@test.com	13800138112	贾芳2	3	1	1	2026-03-12 21:48:44.073014	\N	1	f	2026-03-12 21:48:44.073974	2026-03-12 21:48:44.074027	1
27	甲方测试21	$2a$10$MKZ3gklBa3Kzzm1lx9fh2Om99hUM8.4VGfrW/BvHqu5uRxUkPMJu6	jiafang11@test.com	13800138111	贾芳21	3	1	1	2026-03-12 22:49:59.380488	\N	1	f	2026-03-12 22:49:59.385207	2026-03-12 22:49:59.386876	1
28	甲方测试22	$2a$10$RspIj9I5VgDTXCWAtSiBLuG/c1S4o/TDyKPDaQjENEcgMOl66b6ka	jiafang12@test.com	13800138112	贾芳22	3	1	1	2026-03-12 22:49:59.474377	\N	1	f	2026-03-12 22:49:59.475317	2026-03-12 22:49:59.475712	1
3	jiafang_test	$2b$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy	jiafang@test.com	13900139000	甲方测试	3	1	\N	\N	\N	1	f	2026-03-10 18:41:31.771107	2026-03-10 18:41:31.771107	0
29	甲方测试23	$2a$10$yALMK6dachvm6xITx7jpUOoPb09QKOt2lYRJrvspsHTBWD9FDgmRe	jiafang13@test.com	13800138003	贾芳23	3	1	1	2026-03-12 22:49:59.562371	\N	1	f	2026-03-12 22:49:59.56361	2026-03-12 22:49:59.563679	1
30	甲方测试31	$2a$10$r.nC2aqK8BKPuJfjbDP4AOrZk06cmhA9qCRmv1IyrjRuRwhQJjqZK	jiafang11@test.com	13800138111	贾芳31	3	1	1	2026-03-12 23:14:00.259723	\N	1	f	2026-03-12 23:14:00.263915	2026-03-12 23:14:00.265553	1
5	yifang_user_test	$2a$10$vEhqNCr2rk3mNsl/JRlxbOb0e3GQ5h.YNHDTAaweLj1TsDngkdzxe	yifang_user@test.com	13800138002	乙方普通用户测试	6	1	4	2026-03-11 08:38:30.981156	\N	1	f	2026-03-11 08:38:30.982197	2026-03-11 08:38:30.982312	1
31	甲方测试32	$2a$10$FwBqj8TArkstd2xMb/epfeOOheir7ZAEDBoGYYx9wv7EYbBPk6AAi	jiafang12@test.com	13800138112	贾芳32	3	1	1	2026-03-12 23:14:00.354998	\N	1	f	2026-03-12 23:14:00.355892	2026-03-12 23:14:00.355942	1
9	yifang_admin_1773240322	$2a$10$f3.YlQD.26pCWNhNeU246.90RbaIwhv8TiiTMj7/14PpV7YeotVWu	yifang_admin_1773240322@test.com	13800131773240322	乙方管理员	6	1	1	2026-03-11 22:45:22.966322	\N	1	f	2026-03-11 22:45:22.96699	2026-03-11 22:45:22.967034	0
10	yifang_user_1773240322	$2a$10$ylWFASSKi3n93XwVPb/TqOMygL99kn8ujMYu6nSn1P0NeSJjnKAJm	yifang_user_1773240322@test.com	13800141773240322	乙方用户	6	1	9	2026-03-11 22:45:23.243842	\N	1	f	2026-03-11 22:45:23.244618	2026-03-11 22:45:23.244666	0
12	batch_1773240322_1	$2a$10$eI7kymGS.yFNF5lupd/fsupYHgP6f0Gt/ECuel8TCw60TvtAmP3Wm	batch17732403221@test.com	13800161773240322	批量用户 1	6	1	9	2026-03-11 22:45:23.761782	\N	1	f	2026-03-11 22:45:23.762678	2026-03-11 22:45:23.762741	0
13	batch_1773240322_2	$2a$10$cSFvbXy2yEeorvzTMeiR8OI5uVwqbTNz92h4oRMWLr6k.7wnvn8o.	batch17732403222@test.com	13800171773240322	批量用户 2	6	1	9	2026-03-11 22:45:23.856035	\N	1	f	2026-03-11 22:45:23.856949	2026-03-11 22:45:23.856994	0
32	甲方测试33	$2a$10$zFB1uFYtlROHBoMO1Da22ekyeCri05pXnpX9KcYyZCOx2fn5rNU5S	jiafang13@test.com	13800138003	贾芳33	3	1	1	2026-03-12 23:14:00.441123	\N	1	f	2026-03-12 23:14:00.442003	2026-03-12 23:14:00.442062	1
23	甲方测试13	$2a$10$ItXZ47SjVbcsmipFoSc64uB83Q2AHEtmY257/srDdmAYELNodQoha	jiafang13@test.com	13800138003	贾芳3	3	1	1	2026-03-12 21:41:12.111063	\N	1	f	2026-03-12 21:41:12.115713	2026-03-12 21:41:12.117153	1
33	jiafang_test11	$2a$10$gah2anWx6Un8zL2LTC6tIOo7At5ULpLqKsxaxl0JZH4hV7kTwmB/K	jiafangtest11@test.com	13600138111	贾雨村11	11	1	4	2026-03-12 23:17:56.393351	\N	1	f	2026-03-12 23:17:56.394379	2026-03-12 23:17:56.394433	1
34	jiafang_test12	$2a$10$nyIFMls3LTsTGD4rq31EyOWnYaAdBfgxM6TCmzSimT6OIPY/9b60S	jiafangtest12@test.com	13600138112	贾雨村12	11	1	4	2026-03-12 23:17:56.478345	\N	1	f	2026-03-12 23:17:56.479323	2026-03-12 23:17:56.479386	1
35	jiafang_test13	$2a$10$jNt.PGfK6ZqoEiYbpf0A4.ENLpGK13HaPrmrIYFaoh/kSaMgHq0.S	jiafangtest13@test.com	13600138113	贾雨村13	11	1	4	2026-03-12 23:17:56.564668	\N	1	f	2026-03-12 23:17:56.567702	2026-03-12 23:17:56.567771	1
37	jiafang_test21	$2a$10$kJpXeWHtUy1.O9MySZhZ5e4KCSaEhng2S0s4C9jOxfEDc7ttUaCVu	jiafangtest11@test.com	13600138111	贾雨村21	11	1	4	2026-03-12 23:41:25.78013	\N	1	f	2026-03-12 23:41:25.784433	2026-03-12 23:41:25.785925	1
38	jiafang_test23	$2a$10$KV1fgJwltJK1bMywsJ3dr.Hm3VjxwRU9tCdDCXCysQBGUVQhNdiUq	jiafangtest13@test.com	13600138113	贾雨村23	11	1	4	2026-03-12 23:41:25.880612	\N	1	f	2026-03-12 23:41:25.881551	2026-03-12 23:41:25.881614	1
39	jiafang_test31	$2a$10$vmJu9vnhilaJOFneYXrUcODvXQZgngFLs18ecnjjtCqUVJNW0w.JK	jiafangtest11@test.com	13600138111	贾雨村31	11	1	4	2026-03-13 00:36:37.418419	\N	1	f	2026-03-13 00:36:37.421855	2026-03-13 00:36:37.42365	1
40	jiafang_test33	$2a$10$W6HZaAE2O/ZkDiJlOBlJp.tnd2Wx2WPC.GHvXm1ABthRZKuo2jPYS	jiafangtest13@test.com	13600138113	贾雨村33	11	1	4	2026-03-13 00:36:37.51101	\N	1	f	2026-03-13 00:36:37.511916	2026-03-13 00:36:37.511963	1
41	jiafang_test41	$2a$10$m4JIy0En0KfKmbEsrEPSEe2rOGD4q2E2JXnb8mp9dyM9OLln8aa6m	jiafangtest11@test.com	13600138111	贾雨村41	6	1	1	2026-03-13 00:51:17.79278	\N	1	f	2026-03-13 00:51:17.797379	2026-03-13 00:51:17.798744	1
4	yifang_admin_test	$2a$10$k.EIFd1j2te8DY/fqZmy5u9rlxtaJ3o8lASuWv0OXb2kN6Htrj4Aq	yifang_admin@test.com	13800138221	乙方管理员测试	6	1	1	2026-03-11 08:38:19.001826	\N	1	f	2026-03-11 08:38:19.032617	2026-03-11 08:38:19.033968	1
14	yifang_test10	$2a$10$nw2iEhbnShdyYY9ta/y6M.PKzwGtpM2SBlV5npa7MEA5/GbunowbW	yifangtest10@test.cn	19900000002	张三	6	1	4	2026-03-12 07:06:58.02658	\N	1	f	2026-03-12 07:06:58.031465	2026-03-12 07:06:58.033077	0
42	乙方测试51	$2a$10$miAadaM9kRcwm9ggUeg2t.E4sMGX69lfPQCl2iGrBDx.0mzPe5QGC	yihuiman51@test.com	18800138111	易会满51	6	1	4	2026-03-13 11:40:02.021946	\N	1	f	2026-03-13 11:40:02.027106	2026-03-13 11:40:02.0292	1
43	乙方测试52	$2a$10$XuvcwFAuR1YtVhMBAhY7UOVKv6FqwBWcAzuIZDNuQUgwxBgIRrrwy	yihuiman52@test.com	18800138112	易会满52	6	1	4	2026-03-13 11:40:02.133842	\N	1	f	2026-03-13 11:40:02.134878	2026-03-13 11:40:02.134954	1
44	乙方测试53	$2a$10$/chbzXXdO.agCWm6CD3ZwO7f.zE6k1h9p/KFBgTTmtz1WzGqDihBe	yihuiman53@test.com	18800138113	易会满53	6	1	4	2026-03-13 11:40:02.224166	\N	1	f	2026-03-13 11:40:02.225108	2026-03-13 11:40:02.225184	1
45	乙方测试63	$2a$10$uXSwtxTog3.1k9/o0znUA.2Y6gmSRFnPOFjO5yoCUulGOoVFMfb0O	yihuiman63@test.com	17800138113	易会满63	6	1	4	2026-03-13 11:43:04.033963	\N	1	f	2026-03-13 11:43:04.034826	2026-03-13 11:43:04.034876	1
8	final_test_user	$2a$10$6WwH6Tbf.wQESs4CsvxzgeDpA3vwhAvrYm7piAT0/nyMpYolDDrGG	final@test.com		最终测试用户	6	1	1	2026-03-11 08:42:28.501966	\N	1	f	2026-03-11 08:42:28.502779	2026-03-11 08:42:28.502829	1
1	admin	$2a$10$ZlKgoQIWKpl5qEKHPRAPRuYnVNRy1PdWdF.VT6Ggxt.4ju2y7qvk6	admin@qidian.com	15727236479	管理员	1	1	\N	\N	\N	1	f	2026-03-10 00:02:52.111773	2026-03-10 00:02:52.111773	1
21	yifang_test11	$2a$10$xTm5Uws3LDRxVSgRYix6tOCbrLAnAgwmV38RpsRDxJvgYVAdOwCO2	123@qq.com	15727236471	张二	6	1	4	2026-03-12 12:28:09.311907		1	f	2026-03-12 12:28:09.316674	2026-03-12 12:28:09.318423	0
22	yjtest01	$2a$10$403otYWa2RB/u/7okBcfFuhI79bV9Ti5Ggnf/RDt/LDgr5oXQ.W5K	111@qq.com	15727236476	yanjun1	3	1	1	2026-03-12 20:19:09.083924		1	f	2026-03-12 20:19:09.085768	2026-03-12 20:19:09.085809	1
46	乙方测试61	$2a$10$.WuriKXzbDIjYT/wE6t1qOZM1WokIBM5Pgc8fbNkvKw/F9jEmQ7MO	yihuiman61@test.com	17800138111	易会满61	6	1	4	2026-03-13 11:44:16.29986	\N	1	f	2026-03-13 11:44:16.300735	2026-03-13 11:44:16.300799	1
47	乙方测试62	$2a$10$G3MFCyNG92VefhVhwqBv/.jgnmNYlS4RDZbf/3llZl1tS0fet5YT2	yihuiman62@test.com	17800138112	易会满62	6	1	4	2026-03-13 11:44:16.385408	\N	1	f	2026-03-13 11:44:16.387725	2026-03-13 11:44:16.387795	1
49	hz_handle	$2a$10$kGA5jSrlkN14l4TU6g.dvenVOdKo98l91xWbI67p2qI44lEQOOo0y	huzhong002@test.com	19912340002	华中用户_TEST002	4	1	1	2026-03-13 12:04:50.001775	\N	1	f	2026-03-13 12:04:50.002774	2026-03-13 12:04:50.00282	1
50	hz_project	$2a$10$6OeJ5vGIUc/u6K4AN11OSu6HZe/1o6NSmLGC8n6ZC.P4u.fe8XjX6	huzhong003@test.com	19912340003	华中用户_TEST003	4	1	1	2026-03-13 12:04:50.082868	\N	1	f	2026-03-13 12:04:50.083413	2026-03-13 12:04:50.083466	1
51	hz_workarea_manager_jm	$2a$10$d8WC2g/LIEuAVEn/LPlEIuCbYoGOqGV/Y.2f47ooMmL3O5ys3mJ2y	huzhong004@test.com	19912340004	华中用户_TEST004	4	1	1	2026-03-13 12:04:50.165415	\N	1	f	2026-03-13 12:04:50.166213	2026-03-13 12:04:50.166252	1
52	hz_workarea_handle_jm	$2a$10$u9Y0r14nkxoHeBv2aI/ljOKjuvW4a8z99HGLWa6b3cUiE4wSeCbAG	huzhong005@test.com	19912340005	华中用户_TEST005	4	1	1	2026-03-13 12:04:50.251699	\N	1	f	2026-03-13 12:04:50.252302	2026-03-13 12:04:50.252355	1
53	hz_workarea_project_jm	$2a$10$EkNZJy/On9oGbh5qW7FVtuKtJ94cw7rm6yNt8jRasRiyLqCsy8f.i	huzhong006@test.com	19912340006	华中用户_TEST006	4	1	1	2026-03-13 12:04:50.332961	\N	1	f	2026-03-13 12:04:50.333591	2026-03-13 12:04:50.333631	1
54	yn_admin	$2a$10$h1/ixmJvxKI5XzmywDwzwuPnOPPZokRWD3Ury7Pe5eTUy7VCFn2qS	yunnan001@test.com	14912340001	云南用户_TEST001	5	1	1	2026-03-13 12:04:50.423153	\N	1	f	2026-03-13 12:04:50.423724	2026-03-13 12:04:50.423761	1
55	yn_handle	$2a$10$KMIjHwlnZd3louVq5P.4h.YIKHH1hCfS03aIPI9Vvr3QgerF1Wwoe	yunnan002@test.com	14912340002	云南用户_TEST002	5	1	1	2026-03-13 12:04:50.501972	\N	1	f	2026-03-13 12:04:50.502547	2026-03-13 12:04:50.502588	1
56	yn_project	$2a$10$CM2rSu2QtOcjfo122B6KQ.dBkJ85LcZnN1LKZR9FOKuwI5wgInDE.	yunnan003@test.com	14912340003	云南用户_TEST003	5	1	1	2026-03-13 12:04:50.580626	\N	1	f	2026-03-13 12:04:50.581457	2026-03-13 12:04:50.581495	1
57	yn_workarea_manager_ws	$2a$10$rFp6Sb18MdQ4P2M4caBlm.rkJH9k9BWPjPtL5xVDLE7KBDZcyZ.Ga	yunnan004@test.com	14912340004	云南用户_TEST004	5	1	1	2026-03-13 12:04:50.660991	\N	1	f	2026-03-13 12:04:50.661549	2026-03-13 12:04:50.661588	1
58	yn_workarea_handle_ws	$2a$10$TaH1c1G8WXASzXoBsQ3iVu.1KWgXaan0rc702nKIv./lfOfyFhBh.	yunnan005@test.com	14912340005	云南用户_TEST005	5	1	1	2026-03-13 12:04:50.741016	\N	1	f	2026-03-13 12:04:50.741489	2026-03-13 12:04:50.741541	1
59	yn_workarea_project_ws	$2a$10$YzUETvDXMzwHSkqA6GrDxeuthpRVrC4C5GcGkuVOfbWT3anu3TwLC	yunnan006@test.com	14912340006	云南用户_TEST006	5	1	1	2026-03-13 12:04:50.828223	\N	1	f	2026-03-13 12:04:50.828821	2026-03-13 12:04:50.828862	1
60	yn_workarea_manager_hh	$2a$10$wo51zjhYoOtkiq7N8240R.volLL.1CGVPV87nny2xn7h/N7yIyPmy	yunnan007@test.com	14912340007	云南用户_TEST007	5	1	1	2026-03-13 12:04:50.908276	\N	1	f	2026-03-13 12:04:50.908722	2026-03-13 12:04:50.908759	1
61	yn_workarea_handle_hh	$2a$10$8eN6JXIkUzaoCSVGTd4yZuJNAyA81Q0VoBz3cmmkfg/A9T3kIxbrS	yunnan008@test.com	14912340008	云南用户_TEST008	5	1	1	2026-03-13 12:04:50.98891	\N	1	f	2026-03-13 12:04:50.989406	2026-03-13 12:04:50.989451	1
62	yn_workarea_project_hh	$2a$10$6EzW6V5BiVA.P0b6p9nH0ehI64Poe/LOhPy6zEGSf3XVYCtZZhvOm	yunnan009@test.com	14912340009	云南用户_TEST009	5	1	1	2026-03-13 12:04:51.069377	\N	1	f	2026-03-13 12:04:51.069805	2026-03-13 12:04:51.069844	1
63	jl_admin_xx	$2a$10$i0BGNzGG1ACS.ZBvUHT6eOT8DeQDH2lN7Sdv1gey6x8D3BN8j5y8.	jianli001@test.com	11912340001	监理用户_TEST001	12	1	1	2026-03-13 12:04:51.148255	\N	1	f	2026-03-13 12:04:51.148703	2026-03-13 12:04:51.148762	1
64	jl_handle_xx	$2a$10$tSm6S40acIs7NrRxYe0Oh.IIUMBVdOHlvM1ZUvtnb2Ug30qN4KJSO	jianli002@test.com	11912340002	监理用户_TEST002	12	1	1	2026-03-13 12:04:51.226388	\N	1	f	2026-03-13 12:04:51.226827	2026-03-13 12:04:51.226864	1
65	jl_project_xx	$2a$10$6CCrspbatMV.72NpNh739uG07K6jEnVEKc0RAhnJoB6oF7M24DZ7W	jianli003@test.com	11912340003	监理用户_TEST003	12	1	1	2026-03-13 12:04:51.305824	\N	1	f	2026-03-13 12:04:51.306309	2026-03-13 12:04:51.306352	1
66	hz_workarea_manager_hh	$2a$10$Ypbcl3OEb6UXSHFioQLAg.uIAr9r7NwIvg.yg2eyHZGPlYBZuC2fW	huzhong007@test.com	19912340007	华中用户_TEST007	4	1	1	2026-03-13 12:06:19.845945	\N	1	f	2026-03-13 12:06:19.846308	2026-03-13 12:06:19.846342	1
67	hz_workarea_handle_hh	$2a$10$VuX52MNZ7aIsW2jmLpTOzOSeut1ochsnACxLvmM1OYBSnPuqLNE0i	huzhong008@test.com	19912340008	华中用户_TEST008	4	1	1	2026-03-13 12:06:19.92832	\N	1	f	2026-03-13 12:06:19.928865	2026-03-13 12:06:19.928903	1
68	hz_workarea_project_hh	$2a$10$OKrhXkQneHzAizA.ptQQt.GaWp.6gYktsTOJdGzX0bE09Dif07XnC	huzhong009@test.com	19912340009	华中用户_TEST009	4	1	1	2026-03-13 12:06:20.005653	\N	1	f	2026-03-13 12:06:20.006201	2026-03-13 12:06:20.00624	1
70	login_usertest02	$2a$10$I691qxtka/tKSmI8Y9b8S.EmLj3Tx0Sbb5kTBDd0oCHIvyrQ.0RNS	Zhaokuangying02@test.cn	13100010001	赵匡胤02	3	1	1	2026-03-14 10:01:41.19046		1	f	2026-03-14 09:18:25.914786	2026-03-14 09:18:25.914861	0
69	login_usertest01	$2a$10$ji6TI1AjPbm6eLKwcQKrmeyb0V/qeMXbKe1CM/dhmFKFZKSCFKzZy	Zhangkuangying01@test.cn	13312345678	赵匡胤01	3	1	1	2026-03-14 10:49:51.06729	\N	1	f	2026-03-14 01:37:21.947496	2026-03-14 01:37:21.949069	0
48	hz_admin	$2a$10$f3pq6v964JaFba5.GE86o.vjursQaoXD5JAqNWNM2im2JWj6bPQu.	huzhong001@test.com	19912340001	华中用户_TEST001	4	1	1	2026-03-13 12:04:49.917398	\N	1	f	2026-03-13 12:04:49.918076	2026-03-13 12:04:49.918121	1
71	yjtest02	$2a$10$LXh0Z6xEUnm/52Q9Igdx7OK0eZkbrKhg9Cy9Kcv4u0jk/GCWWLbXa	121@qq.com		yj	12	1	1	2026-03-14 12:30:23.009028		1	f	2026-03-14 12:30:23.011496	2026-03-14 12:30:23.011594	0
72	yjtest03	$2a$10$DHjYkjUCQJago3Q9HWu09uubXYsWx0wb4VGMRcwkJoHwvHB4DtmPu			yj	4	1	54	2026-03-14 12:38:50.160732	\N	1	f	2026-03-14 12:38:50.165843	2026-03-14 12:38:50.167541	0
73	yjtest04	$2a$10$1lTPvkdsyjKvR3o.JZuQqeMZ9aH6i8.JF0V4wcZHk1Dtv5zs2Ln7C	11111@qq.com	15727236473	rrr	12	1	63	2026-03-14 13:09:28.521135		1	f	2026-03-14 13:09:28.525207	2026-03-14 13:09:28.526493	1
74	login_yjtest02	$2a$10$pwgSaFZGEZQXj1mSihDPk..xGYUbhYew3twCwPmxeWorr01Rglpu6	222@qq.com		yj	3	1	1	2026-03-14 13:32:35.088286	\N	1	f	2026-03-14 13:32:03.851843	2026-03-14 13:32:03.853101	0
75	login_yjtest05	$2a$10$i/51pQri.ykKJ0EHKeYcyOKGNICs6aJQVsU2658CztVaxSgzverbq	333@qq.com	13345676789	yj	12	1	63	2026-03-14 14:09:50.709205	\N	1	f	2026-03-14 14:08:47.973277	2026-03-14 14:08:47.973355	1
\.


--
-- Data for Name: work_areas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.work_areas (id, work_area_name, work_area_code, company_id, leader_name, leader_phone, geographic_range, max_capacity, description, is_system_protected, created_at, updated_at) FROM stdin;
3	德宏作业区	Dehong	5				1000		f	2026-03-10 22:34:50.560078	2026-03-10 22:34:50.561803
4	武汉北作业区	Wuhanbei	4				1000		f	2026-03-10 22:35:16.237049	2026-03-10 22:35:16.237101
5	保山作业区	Baoshan	5				1000		f	2026-03-10 22:45:12.777709	2026-03-10 22:45:12.779497
6	大理作业区	Dali	5				1000		f	2026-03-10 22:45:28.597772	2026-03-10 22:45:28.597846
7	楚雄作业区	Chuxiong	5				1000		f	2026-03-10 22:45:49.00396	2026-03-10 22:45:49.004338
8	昆明作业区	Kunming	5				1000		f	2026-03-10 22:46:13.926027	2026-03-10 22:46:13.926098
9	昆东作业区	Kundong	5				1000		f	2026-03-10 22:46:28.079457	2026-03-10 22:46:28.079542
10	曲靖作业区	Qujing	5				1000		f	2026-03-10 22:46:42.210271	2026-03-10 22:46:42.210335
11	玉溪作业区	Yuxi	5				1000		f	2026-03-10 22:47:00.596775	2026-03-10 22:47:00.59684
12	红河作业区	Honghe	5				1000		f	2026-03-10 22:47:34.409031	2026-03-10 22:47:34.409093
13	文山作业区	Wenshan	5				1000		f	2026-03-10 22:47:52.098742	2026-03-10 22:47:52.098794
14	武汉南作业区	Wuhannan	4				1000		f	2026-03-10 22:48:23.424209	2026-03-10 22:48:23.42427
15	黄冈作业区	Huanggang	4				1000		f	2026-03-10 22:48:40.419641	2026-03-10 22:48:40.419697
16	潜江作业区	Qianjiang	4				1000		f	2026-03-10 22:49:04.464371	2026-03-10 22:49:04.464428
17	洪湖作业区	Honghu	4				1000		f	2026-03-10 22:49:18.182909	2026-03-10 22:49:18.182973
18	荆门作业区	Jingmen	4				1000		f	2026-03-10 22:49:34.013115	2026-03-10 22:49:34.013177
19	襄阳作业区	Xiangyang	4				1000		f	2026-03-10 22:49:51.333844	2026-03-10 22:49:51.333903
20	枣阳作业区	Zaoyang	4				1000		f	2026-03-10 22:50:07.068689	2026-03-10 22:50:07.068747
21	宜昌作业区	Yichang	4				1000		f	2026-03-10 22:50:20.463298	2026-03-10 22:50:20.463348
22	恩施作业区	Enshi	4				1000		f	2026-03-10 22:50:32.607613	2026-03-10 22:50:32.60767
\.


--
-- Name: attribute_templates_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.attribute_templates_id_seq', 1, false);


--
-- Name: backup_records_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.backup_records_id_seq', 1, false);


--
-- Name: companies_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.companies_id_seq', 12, true);


--
-- Name: company_types_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.company_types_id_seq', 5, true);


--
-- Name: component_instances_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.component_instances_id_seq', 1, false);


--
-- Name: component_replacement_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.component_replacement_applications_id_seq', 1, false);


--
-- Name: component_replacement_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.component_replacement_history_id_seq', 1, false);


--
-- Name: component_types_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.component_types_id_seq', 12, true);


--
-- Name: construction_orgs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.construction_orgs_id_seq', 1, false);


--
-- Name: construction_teams_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.construction_teams_id_seq', 1, false);


--
-- Name: device_model_components_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.device_model_components_id_seq', 1, false);


--
-- Name: device_models_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.device_models_id_seq', 1, false);


--
-- Name: dynamic_attr_def_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dynamic_attr_def_id_seq', 1, false);


--
-- Name: dynamic_attr_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dynamic_attr_value_id_seq', 1, false);


--
-- Name: final_settlements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.final_settlements_id_seq', 1, false);


--
-- Name: inspection_records_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.inspection_records_id_seq', 1, false);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifications_id_seq', 1, false);


--
-- Name: operation_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.operation_logs_id_seq', 1, false);


--
-- Name: page_access_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.page_access_logs_id_seq', 1, false);


--
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permissions_id_seq', 30, true);


--
-- Name: points_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.points_id_seq', 1, false);


--
-- Name: progress_payment_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.progress_payment_applications_id_seq', 1, false);


--
-- Name: progress_payment_audits_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.progress_payment_audits_id_seq', 1, false);


--
-- Name: project_authorizations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_authorizations_id_seq', 1, false);


--
-- Name: project_quantities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_quantities_id_seq', 1, false);


--
-- Name: project_sections_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.project_sections_id_seq', 1, false);


--
-- Name: projects_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.projects_id_seq', 1, false);


--
-- Name: risk_rectification_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.risk_rectification_items_id_seq', 1, false);


--
-- Name: role_permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.role_permissions_id_seq', 30, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 21, true);


--
-- Name: subtask_audit_records_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subtask_audit_records_id_seq', 1, false);


--
-- Name: subtask_definitions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subtask_definitions_id_seq', 9, true);


--
-- Name: subtask_instances_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subtask_instances_id_seq', 1, false);


--
-- Name: subtask_scrap_applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subtask_scrap_applications_id_seq', 1, false);


--
-- Name: subtask_scrap_audits_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.subtask_scrap_audits_id_seq', 1, false);


--
-- Name: supervision_weekly_reports_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.supervision_weekly_reports_id_seq', 1, false);


--
-- Name: system_configs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.system_configs_id_seq', 10, true);


--
-- Name: table_structure_registry_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.table_structure_registry_id_seq', 1, false);


--
-- Name: task_assignment_users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.task_assignment_users_id_seq', 1, false);


--
-- Name: task_assignments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.task_assignments_id_seq', 1, false);


--
-- Name: team_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.team_members_id_seq', 1, false);


--
-- Name: user_online_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_online_logs_id_seq', 1, false);


--
-- Name: user_role_requests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_role_requests_id_seq', 1, false);


--
-- Name: user_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_roles_id_seq', 78, true);


--
-- Name: user_work_areas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_work_areas_id_seq', 13, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 75, true);


--
-- Name: work_areas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.work_areas_id_seq', 22, true);


--
-- Name: attribute_templates attribute_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.attribute_templates
    ADD CONSTRAINT attribute_templates_pkey PRIMARY KEY (id);


--
-- Name: backup_records backup_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.backup_records
    ADD CONSTRAINT backup_records_pkey PRIMARY KEY (id);


--
-- Name: companies companies_company_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_company_name_key UNIQUE (company_name);


--
-- Name: companies companies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_pkey PRIMARY KEY (id);


--
-- Name: company_types company_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_types
    ADD CONSTRAINT company_types_pkey PRIMARY KEY (id);


--
-- Name: company_types company_types_type_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.company_types
    ADD CONSTRAINT company_types_type_name_key UNIQUE (type_name);


--
-- Name: component_instances component_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_instances
    ADD CONSTRAINT component_instances_pkey PRIMARY KEY (id);


--
-- Name: component_replacement_applications component_replacement_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_applications
    ADD CONSTRAINT component_replacement_applications_pkey PRIMARY KEY (id);


--
-- Name: component_replacement_history component_replacement_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_pkey PRIMARY KEY (id);


--
-- Name: component_types component_types_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_types
    ADD CONSTRAINT component_types_code_key UNIQUE (code);


--
-- Name: component_types component_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_types
    ADD CONSTRAINT component_types_pkey PRIMARY KEY (id);


--
-- Name: construction_orgs construction_orgs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_pkey PRIMARY KEY (id);


--
-- Name: construction_orgs construction_orgs_section_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_section_id_key UNIQUE (section_id);


--
-- Name: construction_teams construction_teams_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_teams
    ADD CONSTRAINT construction_teams_pkey PRIMARY KEY (id);


--
-- Name: device_model_components device_model_components_device_model_id_component_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_model_components
    ADD CONSTRAINT device_model_components_device_model_id_component_id_key UNIQUE (device_model_id, component_id);


--
-- Name: device_model_components device_model_components_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_model_components
    ADD CONSTRAINT device_model_components_pkey PRIMARY KEY (id);


--
-- Name: device_models device_models_model_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_models
    ADD CONSTRAINT device_models_model_name_key UNIQUE (model_name);


--
-- Name: device_models device_models_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_models
    ADD CONSTRAINT device_models_pkey PRIMARY KEY (id);


--
-- Name: dynamic_attr_def dynamic_attr_def_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_def
    ADD CONSTRAINT dynamic_attr_def_pkey PRIMARY KEY (id);


--
-- Name: dynamic_attr_def dynamic_attr_def_table_id_column_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_def
    ADD CONSTRAINT dynamic_attr_def_table_id_column_name_key UNIQUE (table_id, column_name);


--
-- Name: dynamic_attr_value dynamic_attr_value_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_value
    ADD CONSTRAINT dynamic_attr_value_pkey PRIMARY KEY (id);


--
-- Name: final_settlements final_settlements_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.final_settlements
    ADD CONSTRAINT final_settlements_pkey PRIMARY KEY (id);


--
-- Name: final_settlements final_settlements_settlement_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.final_settlements
    ADD CONSTRAINT final_settlements_settlement_number_key UNIQUE (settlement_number);


--
-- Name: inspection_records inspection_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inspection_records
    ADD CONSTRAINT inspection_records_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: operation_logs operation_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_logs
    ADD CONSTRAINT operation_logs_pkey PRIMARY KEY (id);


--
-- Name: page_access_logs page_access_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.page_access_logs
    ADD CONSTRAINT page_access_logs_pkey PRIMARY KEY (id);


--
-- Name: permissions permissions_permission_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_permission_code_key UNIQUE (permission_code);


--
-- Name: permissions permissions_permission_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_permission_name_key UNIQUE (permission_name);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- Name: point_status point_status_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.point_status
    ADD CONSTRAINT point_status_pkey PRIMARY KEY (point_id);


--
-- Name: points points_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.points
    ADD CONSTRAINT points_pkey PRIMARY KEY (id);


--
-- Name: points points_point_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.points
    ADD CONSTRAINT points_point_code_key UNIQUE (point_code);


--
-- Name: progress_payment_applications progress_payment_applications_application_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_applications
    ADD CONSTRAINT progress_payment_applications_application_number_key UNIQUE (application_number);


--
-- Name: progress_payment_applications progress_payment_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_applications
    ADD CONSTRAINT progress_payment_applications_pkey PRIMARY KEY (id);


--
-- Name: progress_payment_audits progress_payment_audits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_audits
    ADD CONSTRAINT progress_payment_audits_pkey PRIMARY KEY (id);


--
-- Name: project_authorizations project_authorizations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_authorizations
    ADD CONSTRAINT project_authorizations_pkey PRIMARY KEY (id);


--
-- Name: project_quantities project_quantities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_pkey PRIMARY KEY (id);


--
-- Name: project_sections project_sections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections
    ADD CONSTRAINT project_sections_pkey PRIMARY KEY (id);


--
-- Name: projects projects_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_pkey PRIMARY KEY (id);


--
-- Name: risk_rectification_items risk_rectification_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.risk_rectification_items
    ADD CONSTRAINT risk_rectification_items_pkey PRIMARY KEY (id);


--
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (id);


--
-- Name: role_permissions role_permissions_role_id_permission_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_permission_id_key UNIQUE (role_id, permission_id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: roles roles_role_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_role_code_key UNIQUE (role_code);


--
-- Name: roles roles_role_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_role_name_key UNIQUE (role_name);


--
-- Name: subtask_audit_records subtask_audit_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_audit_records
    ADD CONSTRAINT subtask_audit_records_pkey PRIMARY KEY (id);


--
-- Name: subtask_definitions subtask_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_definitions
    ADD CONSTRAINT subtask_definitions_pkey PRIMARY KEY (id);


--
-- Name: subtask_definitions subtask_definitions_subtask_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_definitions
    ADD CONSTRAINT subtask_definitions_subtask_code_key UNIQUE (subtask_code);


--
-- Name: subtask_instances subtask_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_instances
    ADD CONSTRAINT subtask_instances_pkey PRIMARY KEY (id);


--
-- Name: subtask_instances subtask_instances_point_id_subtask_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_instances
    ADD CONSTRAINT subtask_instances_point_id_subtask_id_key UNIQUE (point_id, subtask_id);


--
-- Name: subtask_scrap_applications subtask_scrap_applications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_applications
    ADD CONSTRAINT subtask_scrap_applications_pkey PRIMARY KEY (id);


--
-- Name: subtask_scrap_audits subtask_scrap_audits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_audits
    ADD CONSTRAINT subtask_scrap_audits_pkey PRIMARY KEY (id);


--
-- Name: supervision_weekly_reports supervision_weekly_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.supervision_weekly_reports
    ADD CONSTRAINT supervision_weekly_reports_pkey PRIMARY KEY (id);


--
-- Name: system_configs system_configs_config_key_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_configs
    ADD CONSTRAINT system_configs_config_key_key UNIQUE (config_key);


--
-- Name: system_configs system_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.system_configs
    ADD CONSTRAINT system_configs_pkey PRIMARY KEY (id);


--
-- Name: table_structure_registry table_structure_registry_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_structure_registry
    ADD CONSTRAINT table_structure_registry_pkey PRIMARY KEY (id);


--
-- Name: table_structure_registry table_structure_registry_table_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_structure_registry
    ADD CONSTRAINT table_structure_registry_table_name_key UNIQUE (table_name);


--
-- Name: task_assignment_users task_assignment_users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignment_users
    ADD CONSTRAINT task_assignment_users_pkey PRIMARY KEY (id);


--
-- Name: task_assignment_users task_assignment_users_task_id_user_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignment_users
    ADD CONSTRAINT task_assignment_users_task_id_user_id_key UNIQUE (task_id, user_id);


--
-- Name: task_assignments task_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments
    ADD CONSTRAINT task_assignments_pkey PRIMARY KEY (id);


--
-- Name: team_members team_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT team_members_pkey PRIMARY KEY (id);


--
-- Name: team_members team_members_team_id_user_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT team_members_team_id_user_id_key UNIQUE (team_id, user_id);


--
-- Name: user_online_logs user_online_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_online_logs
    ADD CONSTRAINT user_online_logs_pkey PRIMARY KEY (id);


--
-- Name: user_role_requests user_role_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_requests
    ADD CONSTRAINT user_role_requests_pkey PRIMARY KEY (id);


--
-- Name: user_role_requests user_role_requests_user_id_role_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_requests
    ADD CONSTRAINT user_role_requests_user_id_role_id_key UNIQUE (user_id, role_id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_user_id_role_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_role_id_key UNIQUE (user_id, role_id);


--
-- Name: user_work_areas user_work_areas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_work_areas
    ADD CONSTRAINT user_work_areas_pkey PRIMARY KEY (id);


--
-- Name: user_work_areas user_work_areas_user_id_work_area_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_work_areas
    ADD CONSTRAINT user_work_areas_user_id_work_area_id_key UNIQUE (user_id, work_area_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: work_areas work_areas_company_id_work_area_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.work_areas
    ADD CONSTRAINT work_areas_company_id_work_area_code_key UNIQUE (company_id, work_area_code);


--
-- Name: work_areas work_areas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.work_areas
    ADD CONSTRAINT work_areas_pkey PRIMARY KEY (id);


--
-- Name: idx_component_instances_point; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_component_instances_point ON public.component_instances USING btree (point_id);


--
-- Name: idx_component_instances_template; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_component_instances_template ON public.component_instances USING btree (template_id);


--
-- Name: idx_dynamic_attr_value_component; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dynamic_attr_value_component ON public.dynamic_attr_value USING btree (component_instance_id);


--
-- Name: idx_dynamic_attr_value_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dynamic_attr_value_status ON public.dynamic_attr_value USING btree (status);


--
-- Name: idx_dynamic_attr_value_subtask; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dynamic_attr_value_subtask ON public.dynamic_attr_value USING btree (subtask_instance_id);


--
-- Name: idx_notifications_is_read; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notifications_is_read ON public.notifications USING btree (is_read);


--
-- Name: idx_notifications_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_notifications_user ON public.notifications USING btree (user_id);


--
-- Name: idx_operation_logs_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_operation_logs_created ON public.operation_logs USING btree (created_at);


--
-- Name: idx_operation_logs_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_operation_logs_user ON public.operation_logs USING btree (user_id);


--
-- Name: idx_points_geom; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_points_geom ON public.points USING gist (geom);


--
-- Name: idx_points_section; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_points_section ON public.points USING btree (section_id);


--
-- Name: idx_points_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_points_status ON public.points USING btree (status);


--
-- Name: idx_points_work_area; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_points_work_area ON public.points USING btree (work_area_id);


--
-- Name: idx_projects_company; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_projects_company ON public.projects USING btree (company_id);


--
-- Name: idx_projects_manager; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_projects_manager ON public.projects USING btree (project_manager_id);


--
-- Name: idx_projects_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_projects_status ON public.projects USING btree (status);


--
-- Name: idx_sections_project; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sections_project ON public.project_sections USING btree (project_id);


--
-- Name: idx_sections_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sections_status ON public.project_sections USING btree (status);


--
-- Name: idx_subtask_audit_point; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_subtask_audit_point ON public.subtask_audit_records USING btree (point_id);


--
-- Name: idx_subtask_audit_subtask; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_subtask_audit_subtask ON public.subtask_audit_records USING btree (subtask_instance_id);


--
-- Name: idx_subtask_instances_point; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_subtask_instances_point ON public.subtask_instances USING btree (point_id);


--
-- Name: idx_subtask_instances_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_subtask_instances_status ON public.subtask_instances USING btree (status);


--
-- Name: idx_user_role_requests_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_role_requests_status ON public.user_role_requests USING btree (status);


--
-- Name: idx_user_role_requests_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_role_requests_user_id ON public.user_role_requests USING btree (user_id);


--
-- Name: idx_user_work_areas_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_work_areas_user ON public.user_work_areas USING btree (user_id);


--
-- Name: idx_user_work_areas_work_area; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_user_work_areas_work_area ON public.user_work_areas USING btree (work_area_id);


--
-- Name: idx_users_company; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_company ON public.users USING btree (company_id);


--
-- Name: idx_users_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_status ON public.users USING btree (status);


--
-- Name: component_types trg_component_insert; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_component_insert AFTER INSERT ON public.component_types FOR EACH ROW EXECUTE FUNCTION public.on_component_insert();


--
-- Name: subtask_instances trg_subtask_update_point_status; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_subtask_update_point_status AFTER INSERT OR DELETE OR UPDATE ON public.subtask_instances FOR EACH ROW EXECUTE FUNCTION public.update_point_status_from_subtasks();


--
-- Name: attribute_templates trg_template_insert; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_template_insert AFTER INSERT ON public.attribute_templates FOR EACH ROW EXECUTE FUNCTION public.on_template_insert();


--
-- Name: companies companies_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.companies
    ADD CONSTRAINT companies_type_id_fkey FOREIGN KEY (type_id) REFERENCES public.company_types(id);


--
-- Name: component_instances component_instances_component_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_instances
    ADD CONSTRAINT component_instances_component_type_id_fkey FOREIGN KEY (component_type_id) REFERENCES public.component_types(id);


--
-- Name: component_instances component_instances_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_instances
    ADD CONSTRAINT component_instances_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id) ON DELETE CASCADE;


--
-- Name: component_instances component_instances_template_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_instances
    ADD CONSTRAINT component_instances_template_id_fkey FOREIGN KEY (template_id) REFERENCES public.attribute_templates(id);


--
-- Name: component_replacement_applications component_replacement_applications_applicant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_applications
    ADD CONSTRAINT component_replacement_applications_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES public.users(id);


--
-- Name: component_replacement_applications component_replacement_applications_component_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_applications
    ADD CONSTRAINT component_replacement_applications_component_instance_id_fkey FOREIGN KEY (component_instance_id) REFERENCES public.component_instances(id);


--
-- Name: component_replacement_applications component_replacement_applications_suggested_template_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_applications
    ADD CONSTRAINT component_replacement_applications_suggested_template_id_fkey FOREIGN KEY (suggested_template_id) REFERENCES public.attribute_templates(id);


--
-- Name: component_replacement_history component_replacement_history_component_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_component_type_id_fkey FOREIGN KEY (component_type_id) REFERENCES public.component_types(id);


--
-- Name: component_replacement_history component_replacement_history_new_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_new_component_id_fkey FOREIGN KEY (new_component_id) REFERENCES public.component_instances(id);


--
-- Name: component_replacement_history component_replacement_history_old_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_old_component_id_fkey FOREIGN KEY (old_component_id) REFERENCES public.component_instances(id);


--
-- Name: component_replacement_history component_replacement_history_operator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_operator_id_fkey FOREIGN KEY (operator_id) REFERENCES public.users(id);


--
-- Name: component_replacement_history component_replacement_history_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.component_replacement_history
    ADD CONSTRAINT component_replacement_history_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: construction_orgs construction_orgs_project_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_project_manager_id_fkey FOREIGN KEY (project_manager_id) REFERENCES public.users(id);


--
-- Name: construction_orgs construction_orgs_quality_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_quality_manager_id_fkey FOREIGN KEY (quality_manager_id) REFERENCES public.users(id);


--
-- Name: construction_orgs construction_orgs_safety_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_safety_manager_id_fkey FOREIGN KEY (safety_manager_id) REFERENCES public.users(id);


--
-- Name: construction_orgs construction_orgs_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.project_sections(id) ON DELETE CASCADE;


--
-- Name: construction_orgs construction_orgs_technical_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_orgs
    ADD CONSTRAINT construction_orgs_technical_manager_id_fkey FOREIGN KEY (technical_manager_id) REFERENCES public.users(id);


--
-- Name: construction_teams construction_teams_org_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.construction_teams
    ADD CONSTRAINT construction_teams_org_id_fkey FOREIGN KEY (org_id) REFERENCES public.construction_orgs(id) ON DELETE CASCADE;


--
-- Name: device_model_components device_model_components_device_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.device_model_components
    ADD CONSTRAINT device_model_components_device_model_id_fkey FOREIGN KEY (device_model_id) REFERENCES public.device_models(id) ON DELETE CASCADE;


--
-- Name: dynamic_attr_def dynamic_attr_def_component_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_def
    ADD CONSTRAINT dynamic_attr_def_component_type_id_fkey FOREIGN KEY (component_type_id) REFERENCES public.component_types(id);


--
-- Name: dynamic_attr_def dynamic_attr_def_table_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_def
    ADD CONSTRAINT dynamic_attr_def_table_id_fkey FOREIGN KEY (table_id) REFERENCES public.table_structure_registry(id);


--
-- Name: dynamic_attr_value dynamic_attr_value_creator_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_value
    ADD CONSTRAINT dynamic_attr_value_creator_id_fkey FOREIGN KEY (creator_id) REFERENCES public.users(id);


--
-- Name: dynamic_attr_value dynamic_attr_value_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dynamic_attr_value
    ADD CONSTRAINT dynamic_attr_value_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: final_settlements final_settlements_approved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.final_settlements
    ADD CONSTRAINT final_settlements_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.users(id);


--
-- Name: final_settlements final_settlements_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.final_settlements
    ADD CONSTRAINT final_settlements_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: inspection_records inspection_records_inspector_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inspection_records
    ADD CONSTRAINT inspection_records_inspector_id_fkey FOREIGN KEY (inspector_id) REFERENCES public.users(id);


--
-- Name: inspection_records inspection_records_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inspection_records
    ADD CONSTRAINT inspection_records_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: inspection_records inspection_records_reviewer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.inspection_records
    ADD CONSTRAINT inspection_records_reviewer_id_fkey FOREIGN KEY (reviewer_id) REFERENCES public.users(id);


--
-- Name: notifications notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: operation_logs operation_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_logs
    ADD CONSTRAINT operation_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: page_access_logs page_access_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.page_access_logs
    ADD CONSTRAINT page_access_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: point_status point_status_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.point_status
    ADD CONSTRAINT point_status_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id) ON DELETE CASCADE;


--
-- Name: points points_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.points
    ADD CONSTRAINT points_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.project_sections(id) ON DELETE CASCADE;


--
-- Name: points points_work_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.points
    ADD CONSTRAINT points_work_area_id_fkey FOREIGN KEY (work_area_id) REFERENCES public.work_areas(id);


--
-- Name: progress_payment_applications progress_payment_applications_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_applications
    ADD CONSTRAINT progress_payment_applications_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: progress_payment_applications progress_payment_applications_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_applications
    ADD CONSTRAINT progress_payment_applications_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.project_sections(id);


--
-- Name: progress_payment_audits progress_payment_audits_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_audits
    ADD CONSTRAINT progress_payment_audits_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.progress_payment_applications(id);


--
-- Name: progress_payment_audits progress_payment_audits_auditor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.progress_payment_audits
    ADD CONSTRAINT progress_payment_audits_auditor_id_fkey FOREIGN KEY (auditor_id) REFERENCES public.users(id);


--
-- Name: project_authorizations project_authorizations_authorized_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_authorizations
    ADD CONSTRAINT project_authorizations_authorized_user_id_fkey FOREIGN KEY (authorized_user_id) REFERENCES public.users(id);


--
-- Name: project_authorizations project_authorizations_granted_by_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_authorizations
    ADD CONSTRAINT project_authorizations_granted_by_user_id_fkey FOREIGN KEY (granted_by_user_id) REFERENCES public.users(id);


--
-- Name: project_authorizations project_authorizations_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_authorizations
    ADD CONSTRAINT project_authorizations_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: project_quantities project_quantities_confirmed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_confirmed_by_fkey FOREIGN KEY (confirmed_by) REFERENCES public.users(id);


--
-- Name: project_quantities project_quantities_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: project_quantities project_quantities_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: project_quantities project_quantities_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.project_sections(id);


--
-- Name: project_quantities project_quantities_subtask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_subtask_id_fkey FOREIGN KEY (subtask_id) REFERENCES public.subtask_definitions(id);


--
-- Name: project_quantities project_quantities_subtask_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_quantities
    ADD CONSTRAINT project_quantities_subtask_instance_id_fkey FOREIGN KEY (subtask_instance_id) REFERENCES public.subtask_instances(id);


--
-- Name: project_sections project_sections_contractor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections
    ADD CONSTRAINT project_sections_contractor_id_fkey FOREIGN KEY (contractor_id) REFERENCES public.companies(id);


--
-- Name: project_sections project_sections_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections
    ADD CONSTRAINT project_sections_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;


--
-- Name: project_sections project_sections_section_leader_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections
    ADD CONSTRAINT project_sections_section_leader_id_fkey FOREIGN KEY (section_leader_id) REFERENCES public.users(id);


--
-- Name: project_sections project_sections_supervisor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.project_sections
    ADD CONSTRAINT project_sections_supervisor_id_fkey FOREIGN KEY (supervisor_id) REFERENCES public.companies(id);


--
-- Name: projects projects_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: projects projects_project_manager_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.projects
    ADD CONSTRAINT projects_project_manager_id_fkey FOREIGN KEY (project_manager_id) REFERENCES public.users(id);


--
-- Name: risk_rectification_items risk_rectification_items_audit_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.risk_rectification_items
    ADD CONSTRAINT risk_rectification_items_audit_id_fkey FOREIGN KEY (audit_id) REFERENCES public.subtask_audit_records(id);


--
-- Name: risk_rectification_items risk_rectification_items_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.risk_rectification_items
    ADD CONSTRAINT risk_rectification_items_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: risk_rectification_items risk_rectification_items_subtask_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.risk_rectification_items
    ADD CONSTRAINT risk_rectification_items_subtask_instance_id_fkey FOREIGN KEY (subtask_instance_id) REFERENCES public.subtask_instances(id);


--
-- Name: role_permissions role_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- Name: role_permissions role_permissions_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: roles roles_company_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_company_type_id_fkey FOREIGN KEY (company_type_id) REFERENCES public.company_types(id);


--
-- Name: subtask_audit_records subtask_audit_records_auditor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_audit_records
    ADD CONSTRAINT subtask_audit_records_auditor_id_fkey FOREIGN KEY (auditor_id) REFERENCES public.users(id);


--
-- Name: subtask_audit_records subtask_audit_records_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_audit_records
    ADD CONSTRAINT subtask_audit_records_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: subtask_audit_records subtask_audit_records_subtask_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_audit_records
    ADD CONSTRAINT subtask_audit_records_subtask_instance_id_fkey FOREIGN KEY (subtask_instance_id) REFERENCES public.subtask_instances(id);


--
-- Name: subtask_definitions subtask_definitions_predecessor_subtask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_definitions
    ADD CONSTRAINT subtask_definitions_predecessor_subtask_id_fkey FOREIGN KEY (predecessor_subtask_id) REFERENCES public.subtask_definitions(id);


--
-- Name: subtask_instances subtask_instances_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_instances
    ADD CONSTRAINT subtask_instances_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id) ON DELETE CASCADE;


--
-- Name: subtask_instances subtask_instances_subtask_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_instances
    ADD CONSTRAINT subtask_instances_subtask_id_fkey FOREIGN KEY (subtask_id) REFERENCES public.subtask_definitions(id);


--
-- Name: subtask_scrap_applications subtask_scrap_applications_applicant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_applications
    ADD CONSTRAINT subtask_scrap_applications_applicant_id_fkey FOREIGN KEY (applicant_id) REFERENCES public.users(id);


--
-- Name: subtask_scrap_applications subtask_scrap_applications_point_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_applications
    ADD CONSTRAINT subtask_scrap_applications_point_id_fkey FOREIGN KEY (point_id) REFERENCES public.points(id);


--
-- Name: subtask_scrap_applications subtask_scrap_applications_subtask_instance_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_applications
    ADD CONSTRAINT subtask_scrap_applications_subtask_instance_id_fkey FOREIGN KEY (subtask_instance_id) REFERENCES public.subtask_instances(id);


--
-- Name: subtask_scrap_audits subtask_scrap_audits_application_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_audits
    ADD CONSTRAINT subtask_scrap_audits_application_id_fkey FOREIGN KEY (application_id) REFERENCES public.subtask_scrap_applications(id);


--
-- Name: subtask_scrap_audits subtask_scrap_audits_auditor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subtask_scrap_audits
    ADD CONSTRAINT subtask_scrap_audits_auditor_id_fkey FOREIGN KEY (auditor_id) REFERENCES public.users(id);


--
-- Name: table_structure_registry table_structure_registry_component_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.table_structure_registry
    ADD CONSTRAINT table_structure_registry_component_type_id_fkey FOREIGN KEY (component_type_id) REFERENCES public.component_types(id);


--
-- Name: task_assignment_users task_assignment_users_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignment_users
    ADD CONSTRAINT task_assignment_users_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task_assignments(id) ON DELETE CASCADE;


--
-- Name: task_assignment_users task_assignment_users_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignment_users
    ADD CONSTRAINT task_assignment_users_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: task_assignments task_assignments_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments
    ADD CONSTRAINT task_assignments_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.projects(id);


--
-- Name: task_assignments task_assignments_section_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments
    ADD CONSTRAINT task_assignments_section_id_fkey FOREIGN KEY (section_id) REFERENCES public.project_sections(id);


--
-- Name: task_assignments task_assignments_team_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments
    ADD CONSTRAINT task_assignments_team_id_fkey FOREIGN KEY (team_id) REFERENCES public.construction_teams(id);


--
-- Name: task_assignments task_assignments_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.task_assignments
    ADD CONSTRAINT task_assignments_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: team_members team_members_team_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT team_members_team_id_fkey FOREIGN KEY (team_id) REFERENCES public.construction_teams(id) ON DELETE CASCADE;


--
-- Name: team_members team_members_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.team_members
    ADD CONSTRAINT team_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_online_logs user_online_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_online_logs
    ADD CONSTRAINT user_online_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_role_requests user_role_requests_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_requests
    ADD CONSTRAINT user_role_requests_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_role_requests user_role_requests_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_role_requests
    ADD CONSTRAINT user_role_requests_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_work_areas user_work_areas_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_work_areas
    ADD CONSTRAINT user_work_areas_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_work_areas user_work_areas_work_area_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_work_areas
    ADD CONSTRAINT user_work_areas_work_area_id_fkey FOREIGN KEY (work_area_id) REFERENCES public.work_areas(id) ON DELETE CASCADE;


--
-- Name: users users_approved_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_approved_by_fkey FOREIGN KEY (approved_by) REFERENCES public.users(id);


--
-- Name: users users_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- Name: work_areas work_areas_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.work_areas
    ADD CONSTRAINT work_areas_company_id_fkey FOREIGN KEY (company_id) REFERENCES public.companies(id);


--
-- PostgreSQL database dump complete
--

\unrestrict uCZ11gMUbhG4TbanwBcT7ogRCPvfDQh9aLFPEy2LqC1s5gW9mlTyRnjd580KnRq

